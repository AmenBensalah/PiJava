from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd

from config import load_settings
from src.utils import form_score, normalize_game_type, normalize_result, result_score, safe_float, streak_from_end

GAME_KEYS = ["fps", "sports", "battle_royale", "mind", "other"]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build train and predict feature datasets from raw events.")
    parser.add_argument("--output-dir", type=str, default=None)
    parser.add_argument("--limit-users", type=int, default=None)
    parser.add_argument("--user-id", type=int, default=None)
    return parser.parse_args()


def _is_squad(value: str) -> int:
    t = (value or "").strip().lower()
    markers = ["squad", "team", "duo", "trio", "5v5", "4v4", "3v3", "2v2"]
    return int(any(m in t for m in markers))


def _rate(count: int, total: int) -> float:
    return (count / total) if total > 0 else 0.0


def _build_features_from_history(history: pd.DataFrame, game_key: str) -> dict[str, float | int]:
    hist = history.copy()
    valid_results = hist[hist["result"].isin(["W", "D", "L"])]
    prev_matches = len(valid_results)

    wins = int((valid_results["result"] == "W").sum()) if prev_matches else 0
    draws = int((valid_results["result"] == "D").sum()) if prev_matches else 0
    losses = int((valid_results["result"] == "L").sum()) if prev_matches else 0
    avg_points = float(valid_results["points"].mean()) if prev_matches else 0.0
    results_list = valid_results["result"].tolist()

    game_hist = valid_results[valid_results["game_key"] == game_key]
    prev_game_matches = len(game_hist)
    gwins = int((game_hist["result"] == "W").sum()) if prev_game_matches else 0
    gdraws = int((game_hist["result"] == "D").sum()) if prev_game_matches else 0
    glosses = int((game_hist["result"] == "L").sum()) if prev_game_matches else 0
    gavg_points = float(game_hist["points"].mean()) if prev_game_matches else 0.0
    game_results_list = game_hist["result"].tolist()

    out: dict[str, float | int] = {
        "prev_matches": prev_matches,
        "prev_win_rate": _rate(wins, prev_matches),
        "prev_draw_rate": _rate(draws, prev_matches),
        "prev_loss_rate": _rate(losses, prev_matches),
        "prev_avg_points": avg_points,
        "prev_form5_score": form_score(results_list, 5),
        "prev_form10_score": form_score(results_list, 10),
        "prev_recent_win_streak": streak_from_end(results_list, "W"),
        "prev_recent_loss_streak": streak_from_end(results_list, "L"),
        "prev_game_matches": prev_game_matches,
        "prev_game_win_rate": _rate(gwins, prev_game_matches),
        "prev_game_draw_rate": _rate(gdraws, prev_game_matches),
        "prev_game_loss_rate": _rate(glosses, prev_game_matches),
        "prev_game_avg_points": gavg_points,
        "prev_game_form5_score": form_score(game_results_list, 5),
    }
    for key in GAME_KEYS:
        out[f"game_{key}"] = int(key == game_key)
    return out


def main() -> None:
    args = parse_args()
    settings = load_settings(args.output_dir)
    output_dir: Path = settings.output_dir

    raw_path = output_dir / "raw_events.csv"
    if not raw_path.exists() or raw_path.stat().st_size == 0:
        pd.DataFrame().to_csv(output_dir / "train.csv", index=False)
        pd.DataFrame().to_csv(output_dir / "predict.csv", index=False)
        print("No raw events found. Wrote empty train.csv and predict.csv")
        return

    df = pd.read_csv(raw_path)
    if df.empty:
        df.to_csv(output_dir / "train.csv", index=False)
        df.to_csv(output_dir / "predict.csv", index=False)
        print("raw_events.csv is empty. Wrote empty train.csv and predict.csv")
        return

    if args.user_id is not None:
        df = df[df["user_id"] == args.user_id]
    if args.limit_users:
        allowed = df["user_id"].drop_duplicates().head(args.limit_users)
        df = df[df["user_id"].isin(allowed)]

    df["result"] = df["result"].apply(normalize_result)
    df["game_key"] = df["type_game"].apply(normalize_game_type)
    df["timestamp_dt"] = pd.to_datetime(df.get("timestamp"), errors="coerce", utc=True)
    df["points"] = df.get("points", 0).apply(lambda x: safe_float(x, 0.0))
    df["type_tournoi"] = df.get("type_tournoi", "").fillna("")
    df = df.sort_values(["user_id", "timestamp_dt", "match_id"], kind="stable")

    train_rows: list[dict] = []
    predict_rows: list[dict] = []

    for user_id, g in df.groupby("user_id", sort=False):
        user_hist = g.reset_index(drop=True)
        valid_mask = user_hist["result"].isin(["W", "D", "L"])

        for idx, row in user_hist[valid_mask].iterrows():
            history = user_hist.iloc[:idx]
            game_key = row["game_key"]
            feat = _build_features_from_history(history, game_key)
            feat["is_squad"] = _is_squad(str(row.get("type_tournoi", "")))
            feat["label_win"] = int(row["result"] == "W")
            feat["user_id"] = int(user_id)
            feat["game_key"] = game_key
            train_rows.append(feat)

        full_hist = user_hist[user_hist["result"].isin(["W", "D", "L"])]
        for game_key in GAME_KEYS:
            feat = _build_features_from_history(full_hist, game_key)
            feat["is_squad"] = int(_is_squad(str(user_hist["type_tournoi"].iloc[-1])) if not user_hist.empty else 0)
            feat["user_id"] = int(user_id)
            feat["game_key"] = game_key
            feat["sample_size"] = int(feat["prev_game_matches"])
            predict_rows.append(feat)

    train_df = pd.DataFrame(train_rows)
    predict_df = pd.DataFrame(predict_rows)

    if not train_df.empty:
        train_df = train_df.sort_values(["user_id", "game_key"], kind="stable")
    if not predict_df.empty:
        predict_df = predict_df.sort_values(["user_id", "game_key"], kind="stable")

    train_df.to_csv(output_dir / "train.csv", index=False)
    predict_df.to_csv(output_dir / "predict.csv", index=False)

    print(f"Wrote train.csv ({len(train_df)} rows) and predict.csv ({len(predict_df)} rows)")


if __name__ == "__main__":
    main()
