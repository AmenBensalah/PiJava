from __future__ import annotations

import argparse
import json
from pathlib import Path

import pandas as pd

from config import load_settings
from src.utils import normalize_game_type, normalize_result, now_iso, result_score, safe_float

GAME_KEYS = ["fps", "sports", "battle_royale", "mind", "other"]
TOURNAMENT_TYPE_KEYS = ["solo", "duo", "trio", "squad", "other"]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build Symfony-compatible user profile report payload(s).")
    parser.add_argument("--user-id", type=int, default=None)
    parser.add_argument("--all-users", action="store_true", help="Build report files for every detected user")
    parser.add_argument("--output-dir", type=str, default=None)
    parser.add_argument("--limit-users", type=int, default=None)
    parser.add_argument("--predictions-path", type=str, default=None)
    parser.add_argument("--model-info-path", type=str, default=None)
    return parser.parse_args()


def _confidence(matches_played: int) -> str:
    if matches_played >= 20:
        return "high"
    if matches_played >= 8:
        return "medium"
    return "low"


def _trend(recent_results: list[str]) -> str:
    if not recent_results:
        return "insufficient_data"
    score = sum(result_score(x) for x in recent_results)
    if score >= 2:
        return "up"
    if score <= -2:
        return "down"
    return "stable"


def _ai_insight(summary: dict, best_game: str | None, trend: str, placements: dict, confidence: str) -> str:
    matches_played = summary["matchesPlayed"]
    win_rate = summary["winRate"]
    if matches_played == 0:
        return "Pas assez de matchs joues pour generer une analyse IA fiable."

    parts = []
    if win_rate >= 65:
        parts.append("Performance globale solide avec une forte capacite a convertir les matchs en victoires.")
    elif win_rate >= 45:
        parts.append("Profil equilibre avec des performances correctes et une marge de progression.")
    else:
        parts.append("Resultats actuellement fragiles; un travail cible sur la regularite est recommande.")

    if best_game:
        parts.append(f"Le meilleur rendement apparait sur le mode {best_game}.")

    if trend == "up":
        parts.append("La dynamique recente est positive.")
    elif trend == "down":
        parts.append("La dynamique recente est en baisse.")
    elif trend == "stable":
        parts.append("La dynamique recente est stable.")

    if placements.get("first", 0) > 0:
        parts.append(f"{placements['first']} premiere(s) place(s) confirment un vrai potentiel de podium.")

    if confidence == "low":
        parts.append("Attention: faible volume de matchs, analyse a confirmer avec plus de donnees.")

    return " ".join(parts)


def _normalize_tournament_type(value) -> str:
    text = str(value or "").strip().lower().replace("-", " ").replace("_", " ")
    if text in {"solo", "single", "1v1"}:
        return "solo"
    if text in {"duo", "2v2"}:
        return "duo"
    if text in {"trio", "3v3"}:
        return "trio"
    if "squad" in text or text in {"team", "5v5", "4v4"}:
        return "squad"
    return "other"


def _placement_counts(series: pd.Series) -> dict[str, int]:
    text = series.astype(str).str.strip().str.lower()
    first = int(((series == 1) | (text == "1") | (text == "first") | (text == "premier")).sum())
    second = int(((series == 2) | (text == "2") | (text == "second") | (text == "deuxieme") | (text == "deuxième")).sum())
    third = int(((series == 3) | (text == "3") | (text == "third") | (text == "troisieme") | (text == "troisième")).sum())
    return {"first": first, "second": second, "third": third}


def _placement_value(raw_value):
    if pd.isna(raw_value):
        return None
    as_num = pd.to_numeric(pd.Series([raw_value]), errors="coerce").iloc[0]
    if pd.notna(as_num):
        return int(as_num)
    return str(raw_value)


def _empty_report(predictions: dict, model_info: dict, user_id: int) -> dict:
    by_game = {}
    for g in GAME_KEYS:
        by_game[g] = {
            "winProbability": 50.0,
            "expectedResult": "L",
            "confidence": "low",
            "samplesSeen": 0,
            "gameSamples": 0,
            "model": "none",
            "gameType": g,
        }

    user_ml = predictions.get("users", {}).get(str(user_id), {}) if isinstance(predictions, dict) else {}
    if not user_ml:
        user_ml = {"byGameType": by_game, "bestGameType": "other", "bestWinProbability": 50.0}

    summary = {
        "tournoisPlayed": 0,
        "matchesPlayed": 0,
        "wins": 0,
        "draws": 0,
        "losses": 0,
        "winRate": 0.0,
        "totalPoints": 0.0,
        "averagePointsPerMatch": 0.0,
        "bestGameType": None,
        "bestGameWinRate": 0.0,
    }
    placements = {"first": 0, "second": 0, "third": 0}
    per_game = {
        k: {"played": 0, "wins": 0, "draws": 0, "losses": 0, "points": 0.0, "winRate": 0.0, "avgPoints": 0.0}
        for k in GAME_KEYS
    }
    tournament_type_stats = {
        k: {"played": 0, "wins": 0, "draws": 0, "losses": 0, "winRate": 0.0}
        for k in TOURNAMENT_TYPE_KEYS
    }
    tournament_type_prediction = {
        k: {"predictedWinRate": 50.0, "confidence": "low", "historySamples": 0}
        for k in TOURNAMENT_TYPE_KEYS
    }

    return {
        "generatedAt": now_iso(),
        "summary": summary,
        "placements": placements,
        "perGame": per_game,
        "recentForm": [],
        "recentMatches": [],
        "trend": "insufficient_data",
        "confidence": "low",
        "dataQuality": {
            "matchedByPlayerLink": 0,
            "matchedByNameAlias": 0,
            "matchedByPlacement": 0,
            "ambiguousSideMatches": 0,
        },
        "aiInsight": _ai_insight(summary, None, "insufficient_data", placements, "low"),
        "tournamentTypeStats": tournament_type_stats,
        "tournamentTypePrediction": tournament_type_prediction,
        "mlPrediction": user_ml,
        "mlModelInfo": model_info,
    }


def _build_user_report(df: pd.DataFrame, predictions: dict, model_info: dict, user_id: int) -> dict:
    user_df = df[df["user_id"] == user_id].copy() if not df.empty else pd.DataFrame()
    if user_df.empty:
        return _empty_report(predictions, model_info, user_id)

    user_df["result"] = user_df["result"].apply(normalize_result)
    user_df["game_key"] = user_df["type_game"].apply(normalize_game_type)
    user_df["tournament_key"] = user_df["type_tournoi"].apply(_normalize_tournament_type)
    user_df["points"] = user_df.get("points", 0).apply(lambda x: safe_float(x, 0.0))
    user_df["timestamp_dt"] = pd.to_datetime(user_df.get("timestamp"), errors="coerce", utc=True)
    user_df = user_df.sort_values(["timestamp_dt", "match_id"], kind="stable")

    unique_tournois = user_df["tournoi_id"].dropna().nunique() if "tournoi_id" in user_df.columns else 0
    unique_matches = user_df["match_id"].dropna().nunique() if "match_id" in user_df.columns else len(user_df)

    w = int((user_df["result"] == "W").sum())
    d = int((user_df["result"] == "D").sum())
    l = int((user_df["result"] == "L").sum())
    decided = w + d + l
    total_points = float(user_df["points"].sum())
    avg_points = float(total_points / unique_matches) if unique_matches else 0.0

    per_game = {}
    best_game = None
    best_game_rate = -1.0
    for g in GAME_KEYS:
        sub = user_df[user_df["game_key"] == g]
        played = int(len(sub))
        gw = int((sub["result"] == "W").sum())
        gd = int((sub["result"] == "D").sum())
        gl = int((sub["result"] == "L").sum())
        gpoints = float(sub["points"].sum())
        wr = float((gw / played) * 100.0) if played else 0.0
        agp = float(gpoints / played) if played else 0.0
        per_game[g] = {
            "played": played,
            "wins": gw,
            "draws": gd,
            "losses": gl,
            "points": round(gpoints, 2),
            "winRate": round(wr, 1),
            "avgPoints": round(agp, 2),
        }
        if played > 0 and wr > best_game_rate:
            best_game = g
            best_game_rate = wr

    tournament_type_stats = {}
    for key in TOURNAMENT_TYPE_KEYS:
        sub = user_df[user_df["tournament_key"] == key]
        played = int(len(sub))
        tw = int((sub["result"] == "W").sum())
        td = int((sub["result"] == "D").sum())
        tl = int((sub["result"] == "L").sum())
        win_rate = float((tw / played) * 100.0) if played else 0.0
        tournament_type_stats[key] = {
            "played": played,
            "wins": tw,
            "draws": td,
            "losses": tl,
            "winRate": round(win_rate, 1),
        }

    summary = {
        "tournoisPlayed": int(unique_tournois),
        "matchesPlayed": int(unique_matches),
        "wins": w,
        "draws": d,
        "losses": l,
        "winRate": round((w / decided) * 100.0, 1) if decided else 0.0,
        "totalPoints": round(total_points, 2),
        "averagePointsPerMatch": round(avg_points, 2),
        "bestGameType": best_game,
        "bestGameWinRate": round(best_game_rate if best_game_rate > 0 else 0.0, 1),
    }

    placement_series = user_df.get("placement", pd.Series(dtype=object))
    placements = _placement_counts(placement_series)

    valid_res = [x for x in user_df["result"].tolist() if x in {"W", "D", "L"}]
    recent_form = valid_res[-5:]

    recent_rows = user_df.tail(12)
    recent_matches = []
    for _, r in recent_rows.iterrows():
        recent_matches.append(
            {
                "date": r.get("date"),
                "tournoi": r.get("tournoi_name") or r.get("tournoi_id"),
                "typeGame": r.get("game_key"),
                "typeTournoi": r.get("type_tournoi"),
                "result": r.get("result"),
                "points": round(float(r.get("points", 0.0)), 2),
                "placement": _placement_value(r.get("placement")),
                "matchedBy": r.get("matched_by"),
            }
        )

    trend = _trend(recent_form)
    confidence = _confidence(summary["matchesPlayed"])

    data_quality = {
        "matchedByPlayerLink": int((user_df["matched_by"] == "player_link").sum()) if "matched_by" in user_df.columns else 0,
        "matchedByNameAlias": int((user_df["matched_by"] == "name_alias").sum()) if "matched_by" in user_df.columns else 0,
        "matchedByPlacement": int((user_df["matched_by"] == "placement").sum()) if "matched_by" in user_df.columns else 0,
        "ambiguousSideMatches": int((user_df["matched_by"] == "ambiguous").sum()) if "matched_by" in user_df.columns else 0,
    }

    user_ml = predictions.get("users", {}).get(str(user_id), {}) if isinstance(predictions, dict) else {}
    if not user_ml:
        user_ml = _empty_report(predictions, model_info, user_id)["mlPrediction"]

    by_game = user_ml.get("byGameType", {}) if isinstance(user_ml, dict) else {}
    game_probs = [safe_float(v.get("winProbability"), 50.0) for v in by_game.values() if isinstance(v, dict)]
    ml_baseline = float(sum(game_probs) / len(game_probs)) if game_probs else 50.0

    tournament_type_prediction = {}
    for key in TOURNAMENT_TYPE_KEYS:
        hist = tournament_type_stats.get(key, {})
        hist_played = int(hist.get("played", 0))
        hist_rate = float(hist.get("winRate", 0.0))
        # Blend historical tournament-type rate with ML baseline.
        weight = min(hist_played / 10.0, 1.0)
        pred_rate = round(weight * hist_rate + (1.0 - weight) * ml_baseline, 1)
        tournament_type_prediction[key] = {
            "predictedWinRate": pred_rate,
            "confidence": _confidence(hist_played),
            "historySamples": hist_played,
        }

    return {
        "generatedAt": now_iso(),
        "summary": summary,
        "placements": placements,
        "perGame": per_game,
        "recentForm": recent_form,
        "recentMatches": recent_matches,
        "trend": trend,
        "confidence": confidence,
        "dataQuality": data_quality,
        "aiInsight": _ai_insight(summary, summary.get("bestGameType"), trend, placements, confidence),
        "tournamentTypeStats": tournament_type_stats,
        "tournamentTypePrediction": tournament_type_prediction,
        "mlPrediction": user_ml,
        "mlModelInfo": model_info,
    }


def main() -> None:
    args = parse_args()
    settings = load_settings(args.output_dir)
    output_dir: Path = settings.output_dir

    raw_path = output_dir / "raw_events.csv"
    pred_path = Path(args.predictions_path) if args.predictions_path else output_dir / "predictions.json"
    model_path = Path(args.model_info_path) if args.model_info_path else output_dir / "model_info.json"

    df = pd.read_csv(raw_path) if raw_path.exists() and raw_path.stat().st_size > 0 else pd.DataFrame()

    predictions = {}
    if pred_path.exists() and pred_path.stat().st_size > 0:
        with pred_path.open("r", encoding="utf-8") as fh:
            predictions = json.load(fh)

    model_info = {}
    if model_path.exists() and model_path.stat().st_size > 0:
        with model_path.open("r", encoding="utf-8") as fh:
            model_info = json.load(fh)

    # Determine which users to build.
    if args.user_id is not None and not args.all_users:
        user_ids = [args.user_id]
    else:
        users_from_raw = set(int(x) for x in df["user_id"].dropna().unique().tolist()) if (not df.empty and "user_id" in df.columns) else set()
        users_from_pred = set(int(k) for k in predictions.get("users", {}).keys()) if isinstance(predictions, dict) else set()
        user_ids = sorted(users_from_raw | users_from_pred)

    if args.limit_users:
        user_ids = user_ids[: args.limit_users]

    if not user_ids:
        out_empty = output_dir / "profile_reports_all.json"
        payload = {"generatedAt": now_iso(), "users": {}}
        with out_empty.open("w", encoding="utf-8") as fh:
            json.dump(payload, fh, ensure_ascii=False, indent=2)
        print(json.dumps(payload, ensure_ascii=False, indent=2))
        print(f"Saved empty aggregate report to {out_empty}")
        return

    reports_dir = output_dir / "profile_reports"
    reports_dir.mkdir(parents=True, exist_ok=True)

    all_reports = {}
    for user_id in user_ids:
        report = _build_user_report(df, predictions, model_info, user_id)
        all_reports[str(user_id)] = report

        out_path = reports_dir / f"profile_report_user_{user_id}.json"
        with out_path.open("w", encoding="utf-8") as fh:
            json.dump(report, fh, ensure_ascii=False, indent=2)

    aggregate = {
        "generatedAt": now_iso(),
        "users": all_reports,
    }
    out_agg = output_dir / "profile_reports_all.json"
    with out_agg.open("w", encoding="utf-8") as fh:
        json.dump(aggregate, fh, ensure_ascii=False, indent=2)

    # Backward compatibility single-user file at root output directory.
    if args.user_id is not None and not args.all_users:
        out_single = output_dir / f"profile_report_user_{args.user_id}.json"
        with out_single.open("w", encoding="utf-8") as fh:
            json.dump(all_reports[str(args.user_id)], fh, ensure_ascii=False, indent=2)
        print(json.dumps(all_reports[str(args.user_id)], ensure_ascii=False, indent=2))
        print(f"Saved profile report to {out_single}")

    print(f"Saved {len(all_reports)} user report files in {reports_dir}")
    print(f"Saved aggregate report to {out_agg}")


if __name__ == "__main__":
    main()
