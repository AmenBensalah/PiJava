from __future__ import annotations

import argparse
from collections import defaultdict
from pathlib import Path
from typing import Any

import pandas as pd

from config import load_settings
from src.db import build_engine, get_column_names, pick_column, read_table, resolve_table
from src.utils import normalize_result, normalize_text, now_iso, safe_float


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Extract robust user match events from MySQL.")
    parser.add_argument("--output-dir", type=str, default=None)
    parser.add_argument("--limit-users", type=int, default=None)
    parser.add_argument("--user-id", type=int, default=None)
    return parser.parse_args()


def _accepted(value: Any) -> bool:
    t = normalize_text(value)
    return t in {"1", "true", "accepted", "approve", "approved", "active", "yes", "ok"}


def _build_user_aliases(users_df: pd.DataFrame, teams_by_user: dict[int, set[str]]) -> dict[int, set[str]]:
    aliases: dict[int, set[str]] = defaultdict(set)
    for _, row in users_df.iterrows():
        user_id = int(row["user_id"])
        for source in [row.get("nom"), row.get("pseudo"), row.get("email")]:
            norm = normalize_text(source)
            if not norm:
                continue
            aliases[user_id].add(norm)
            if "@" in norm:
                aliases[user_id].add(norm.split("@", 1)[0])
        aliases[user_id].update(teams_by_user.get(user_id, set()))
    return aliases


def _result_from_side(home_score: float | None, away_score: float | None, side: str | None) -> str | None:
    if home_score is None or away_score is None:
        return None
    if side == "home":
        if home_score > away_score:
            return "W"
        if home_score < away_score:
            return "L"
        return "D"
    if side == "away":
        if away_score > home_score:
            return "W"
        if away_score < home_score:
            return "L"
        return "D"
    return None


def _is_played(status: Any) -> bool:
    t = normalize_text(status)
    return t in {"played", "finished", "done", "complete", "completed", "termine", "terminé", "scored"}


def main() -> None:
    args = parse_args()
    settings = load_settings(args.output_dir)
    output_dir: Path = settings.output_dir
    output_dir.mkdir(parents=True, exist_ok=True)

    engine = build_engine(settings)

    user_table = resolve_table(engine, ["users", "user", "utilisateur", "utilisateurs"])
    if not user_table.name:
        pd.DataFrame().to_csv(output_dir / "raw_events.csv", index=False)
        print(f"No user table found. Wrote empty file: {output_dir / 'raw_events.csv'}")
        return

    user_cols = get_column_names(engine, user_table.name)
    user_id_col = pick_column(user_cols, ["id", "user_id", "uid"])
    nom_col = pick_column(user_cols, ["nom", "full_name", "name", "username"])
    pseudo_col = pick_column(user_cols, ["pseudo", "nickname", "display_name"])
    email_col = pick_column(user_cols, ["email", "mail"])
    select_cols = [c for c in [user_id_col, nom_col, pseudo_col, email_col] if c]

    users_df = read_table(engine, user_table.name, select_cols)
    rename_map = {}
    if user_id_col:
        rename_map[user_id_col] = "user_id"
    if nom_col:
        rename_map[nom_col] = "nom"
    if pseudo_col:
        rename_map[pseudo_col] = "pseudo"
    if email_col:
        rename_map[email_col] = "email"
    users_df = users_df.rename(columns=rename_map)

    if "user_id" not in users_df.columns:
        pd.DataFrame().to_csv(output_dir / "raw_events.csv", index=False)
        print(f"No user id column found. Wrote empty file: {output_dir / 'raw_events.csv'}")
        return

    if args.user_id is not None:
        users_df = users_df[users_df["user_id"] == args.user_id]
    if args.limit_users:
        users_df = users_df.head(args.limit_users)

    users_df = users_df.fillna("")

    teams_by_user: dict[int, set[str]] = defaultdict(set)
    teams_table = resolve_table(engine, ["teams", "team", "equipes", "equipe"])
    memberships_table = resolve_table(engine, ["team_members", "team_member", "team_users", "user_team", "memberships", "membership"])

    if teams_table.name:
        tcols = get_column_names(engine, teams_table.name)
        tid_col = pick_column(tcols, ["id", "team_id"])
        tname_col = pick_column(tcols, ["name", "nom", "team_name"])
        manager_col = pick_column(tcols, ["owner_id", "manager_id", "created_by", "user_id"])
        if tid_col and tname_col:
            teams_df = read_table(engine, teams_table.name, [c for c in [tid_col, tname_col, manager_col] if c]).rename(
                columns={tid_col: "team_id", tname_col: "team_name", manager_col: "manager_user_id" if manager_col else "manager_user_id"}
            )
            for _, row in teams_df.iterrows():
                team_name = normalize_text(row.get("team_name"))
                if team_name and pd.notna(row.get("manager_user_id")):
                    teams_by_user[int(row["manager_user_id"])].add(team_name)
        else:
            teams_df = pd.DataFrame(columns=["team_id", "team_name"])
    else:
        teams_df = pd.DataFrame(columns=["team_id", "team_name"])

    if memberships_table.name and not teams_df.empty:
        mcols = get_column_names(engine, memberships_table.name)
        m_user_col = pick_column(mcols, ["user_id", "id_user", "member_id"])
        m_team_col = pick_column(mcols, ["team_id", "id_team", "groupe_id"])
        m_status_col = pick_column(mcols, ["status", "etat", "is_accepted", "accepted", "approved", "is_active"])
        if m_user_col and m_team_col:
            ms_cols = [c for c in [m_user_col, m_team_col, m_status_col] if c]
            members_df = read_table(engine, memberships_table.name, ms_cols).rename(
                columns={m_user_col: "user_id", m_team_col: "team_id", m_status_col: "status" if m_status_col else "status"}
            )
            if "status" in members_df.columns:
                members_df = members_df[members_df["status"].apply(_accepted)]
            team_name_map = {int(r["team_id"]): normalize_text(r["team_name"]) for _, r in teams_df.iterrows() if pd.notna(r["team_id"]) and normalize_text(r["team_name"]) }
            for _, row in members_df.iterrows():
                user_id = int(row["user_id"])
                team_name = team_name_map.get(int(row["team_id"]))
                if team_name:
                    teams_by_user[user_id].add(team_name)

    aliases_by_user = _build_user_aliases(users_df, teams_by_user)
    alias_to_users: dict[str, set[int]] = defaultdict(set)
    for uid, alias_set in aliases_by_user.items():
        for alias in alias_set:
            alias_to_users[alias].add(uid)

    matches_table = resolve_table(engine, ["matches", "match", "game_matches", "parties", "partie", "tournoi_match"])
    if not matches_table.name:
        pd.DataFrame().to_csv(output_dir / "raw_events.csv", index=False)
        print(f"No matches table found. Wrote empty file: {output_dir / 'raw_events.csv'}")
        return

    mcols = get_column_names(engine, matches_table.name)
    match_id_col = pick_column(mcols, ["id", "match_id"])
    tournoi_id_col = pick_column(mcols, ["tournoi_id", "tournament_id", "competition_id", "event_id"])
    type_game_col = pick_column(mcols, ["type_game", "game_type", "jeu_type"])
    type_tournoi_col = pick_column(mcols, ["type_tournoi", "tournament_type", "format"])
    timestamp_col = pick_column(mcols, ["played_at", "match_date", "date", "scheduled_at", "created_at", "updated_at", "timestamp"])
    status_col = pick_column(mcols, ["status", "etat", "state"])
    home_name_col = pick_column(mcols, ["home_name", "homeName", "playerA_name", "team_a_name", "name_a"])
    away_name_col = pick_column(mcols, ["away_name", "awayName", "playerB_name", "team_b_name", "name_b"])
    player_a_col = pick_column(mcols, ["playerA_id", "player_a_id", "home_player_id", "user_a_id"])
    player_b_col = pick_column(mcols, ["playerB_id", "player_b_id", "away_player_id", "user_b_id"])
    home_score_col = pick_column(mcols, ["home_score", "score_home", "score_a", "playerA_score", "points_a"])
    away_score_col = pick_column(mcols, ["away_score", "score_away", "score_b", "playerB_score", "points_b"])
    home_points_col = pick_column(mcols, ["home_points", "points_home", "points_a"])
    away_points_col = pick_column(mcols, ["away_points", "points_away", "points_b"])

    match_select_cols = [
        c
        for c in [
            match_id_col,
            tournoi_id_col,
            type_game_col,
            type_tournoi_col,
            timestamp_col,
            status_col,
            home_name_col,
            away_name_col,
            player_a_col,
            player_b_col,
            home_score_col,
            away_score_col,
            home_points_col,
            away_points_col,
        ]
        if c
    ]
    matches_df = read_table(engine, matches_table.name, match_select_cols)
    rename_matches = {
        match_id_col: "match_id",
        tournoi_id_col: "tournoi_id",
        type_game_col: "type_game",
        type_tournoi_col: "type_tournoi",
        timestamp_col: "timestamp",
        status_col: "status",
        home_name_col: "home_name",
        away_name_col: "away_name",
        player_a_col: "player_a_id",
        player_b_col: "player_b_id",
        home_score_col: "home_score",
        away_score_col: "away_score",
        home_points_col: "home_points",
        away_points_col: "away_points",
    }
    matches_df = matches_df.rename(columns={k: v for k, v in rename_matches.items() if k})
    if "match_id" not in matches_df.columns:
        matches_df["match_id"] = matches_df.index + 1

    tournoi_name_map: dict[int, str] = {}
    tournoi_type_game_map: dict[int, str] = {}
    tournoi_type_tournoi_map: dict[int, str] = {}
    tournaments_table = resolve_table(engine, ["tournois", "tournoi", "tournaments", "tournament", "competitions"])
    if tournaments_table.name:
        tcols2 = get_column_names(engine, tournaments_table.name)
        tid2 = pick_column(tcols2, ["id", "id_tournoi", "tournoi_id", "tournament_id"])
        tname2 = pick_column(tcols2, ["name", "nom", "title"])
        tgame2 = pick_column(tcols2, ["type_game", "game_type", "jeu_type"])
        ttype2 = pick_column(tcols2, ["type_tournoi", "tournament_type", "format"])
        if tid2:
            cols = [c for c in [tid2, tname2, tgame2, ttype2] if c]
            tdf2 = read_table(engine, tournaments_table.name, cols).rename(
                columns={tid2: "tournoi_id", tname2: "tournoi_name", tgame2: "tournoi_type_game", ttype2: "tournoi_type_tournoi"}
            )
            tournoi_name_map = {
                int(r["tournoi_id"]): str(r.get("tournoi_name", ""))
                for _, r in tdf2.iterrows()
                if pd.notna(r["tournoi_id"]) and str(r.get("tournoi_name", "")).strip()
            }
            tournoi_type_game_map = {
                int(r["tournoi_id"]): str(r.get("tournoi_type_game", ""))
                for _, r in tdf2.iterrows()
                if pd.notna(r["tournoi_id"]) and str(r.get("tournoi_type_game", "")).strip()
            }
            tournoi_type_tournoi_map = {
                int(r["tournoi_id"]): str(r.get("tournoi_type_tournoi", ""))
                for _, r in tdf2.iterrows()
                if pd.notna(r["tournoi_id"]) and str(r.get("tournoi_type_tournoi", "")).strip()
            }

    participants_table = resolve_table(
        engine,
        ["match_participants", "participants", "participant", "match_results", "results", "resultats", "tournoi_match_participant_result"],
    )
    participant_by_match_user: dict[tuple[int, int], dict[str, Any]] = {}
    participant_has_points_by_match: dict[int, bool] = defaultdict(bool)

    if participants_table.name:
        pcols = get_column_names(engine, participants_table.name)
        p_match_col = pick_column(pcols, ["match_id", "id_match"])
        p_user_col = pick_column(pcols, ["user_id", "participant_id", "player_id", "id_user"])
        p_points_col = pick_column(pcols, ["points", "placement_points", "score"])
        p_placement_col = pick_column(pcols, ["placement", "rank", "position"])
        p_result_col = pick_column(pcols, ["result", "outcome"])
        if p_match_col and p_user_col:
            p_select = [c for c in [p_match_col, p_user_col, p_points_col, p_placement_col, p_result_col] if c]
            part_df = read_table(engine, participants_table.name, p_select).rename(
                columns={
                    p_match_col: "match_id",
                    p_user_col: "user_id",
                    p_points_col: "placement_points",
                    p_placement_col: "placement",
                    p_result_col: "result",
                }
            )
            for _, row in part_df.iterrows():
                if pd.isna(row.get("match_id")) or pd.isna(row.get("user_id")):
                    continue
                key = (int(row["match_id"]), int(row["user_id"]))
                participant_by_match_user[key] = {
                    "placement_points": safe_float(row.get("placement_points"), 0.0),
                    "placement": row.get("placement"),
                    "result": normalize_result(row.get("result")),
                }
                if safe_float(row.get("placement_points"), 0.0) > 0:
                    participant_has_points_by_match[int(row["match_id"])] = True

    allowed_user_ids = set(int(x) for x in users_df["user_id"].tolist())

    records: list[dict[str, Any]] = []
    for _, row in matches_df.iterrows():
        match_id = int(row["match_id"])
        status = row.get("status")
        home_score = row.get("home_score")
        away_score = row.get("away_score")
        hs = safe_float(home_score, None) if pd.notna(home_score) else None
        as_ = safe_float(away_score, None) if pd.notna(away_score) else None

        scores_exist = hs is not None and as_ is not None
        played = _is_played(status)
        placement_scored = participant_has_points_by_match.get(match_id, False)
        if not played and not scores_exist and not placement_scored:
            continue

        direct_home_ids: set[int] = set()
        direct_away_ids: set[int] = set()
        if pd.notna(row.get("player_a_id")):
            direct_home_ids.add(int(row["player_a_id"]))
        if pd.notna(row.get("player_b_id")):
            direct_away_ids.add(int(row["player_b_id"]))

        home_alias = normalize_text(row.get("home_name"))
        away_alias = normalize_text(row.get("away_name"))
        alias_home_ids = alias_to_users.get(home_alias, set()) if home_alias else set()
        alias_away_ids = alias_to_users.get(away_alias, set()) if away_alias else set()

        participant_user_ids = {u for (m, u) in participant_by_match_user.keys() if m == match_id}

        candidate_user_ids = (direct_home_ids | direct_away_ids | alias_home_ids | alias_away_ids | participant_user_ids) & allowed_user_ids

        for user_id in candidate_user_ids:
            in_home = user_id in direct_home_ids or user_id in alias_home_ids
            in_away = user_id in direct_away_ids or user_id in alias_away_ids
            side = "home" if in_home and not in_away else "away" if in_away and not in_home else None

            if in_home and in_away:
                matched_by = "ambiguous"
            elif user_id in direct_home_ids or user_id in direct_away_ids:
                matched_by = "player_link"
            elif user_id in alias_home_ids or user_id in alias_away_ids:
                matched_by = "name_alias"
            elif user_id in participant_user_ids:
                matched_by = "placement"
            else:
                matched_by = "none"

            part = participant_by_match_user.get((match_id, user_id), {})
            placement_points = safe_float(part.get("placement_points"), 0.0)
            side_points = 0.0
            if side == "home":
                side_points = safe_float(row.get("home_points"), 0.0)
            elif side == "away":
                side_points = safe_float(row.get("away_points"), 0.0)

            result = _result_from_side(hs, as_, side)
            if not result:
                result = part.get("result")

            raw_ts = row.get("timestamp")
            ts = pd.to_datetime(raw_ts, errors="coerce", utc=True)
            ts_iso = ts.isoformat() if pd.notna(ts) else None
            date_str = ts.date().isoformat() if pd.notna(ts) else None

            tournoi_id = row.get("tournoi_id")
            tournoi_name = tournoi_name_map.get(int(tournoi_id), "") if pd.notna(tournoi_id) else ""
            type_game = row.get("type_game")
            type_tournoi = row.get("type_tournoi")
            if pd.notna(tournoi_id):
                tid = int(tournoi_id)
                if not normalize_text(type_game):
                    type_game = tournoi_type_game_map.get(tid, type_game)
                if not normalize_text(type_tournoi):
                    type_tournoi = tournoi_type_tournoi_map.get(tid, type_tournoi)

            records.append(
                {
                    "user_id": int(user_id),
                    "match_id": match_id,
                    "tournoi_id": int(tournoi_id) if pd.notna(tournoi_id) else None,
                    "tournoi_name": tournoi_name,
                    "type_game": type_game,
                    "type_tournoi": type_tournoi,
                    "timestamp": ts_iso,
                    "date": date_str,
                    "result": result,
                    "points": round(side_points + placement_points, 3),
                    "side_points": round(side_points, 3),
                    "placement_points": round(placement_points, 3),
                    "placement": part.get("placement"),
                    "matched_by": matched_by,
                    "generated_at": now_iso(),
                }
            )

    raw_events = pd.DataFrame(records)
    if not raw_events.empty:
        raw_events = raw_events.sort_values(["user_id", "timestamp", "match_id"], kind="stable")
    raw_events.to_csv(output_dir / "raw_events.csv", index=False)
    print(f"Wrote raw events to {output_dir / 'raw_events.csv'} ({len(raw_events)} rows)")


if __name__ == "__main__":
    main()
