from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.ensemble import HistGradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import balanced_accuracy_score
from sklearn.model_selection import StratifiedKFold, cross_val_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from config import load_settings
from src.utils import clamp, now_iso, to_json_file

GAME_KEYS = ["fps", "sports", "battle_royale", "mind", "other"]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Train user performance model and write predictions.")
    parser.add_argument("--output-dir", type=str, default=None)
    parser.add_argument("--limit-users", type=int, default=None)
    parser.add_argument("--user-id", type=int, default=None)
    return parser.parse_args()


def confidence_from_samples(n: int) -> str:
    if n >= 20:
        return "high"
    if n >= 8:
        return "medium"
    return "low"


def build_empty_predictions(predict_df: pd.DataFrame) -> dict:
    users_payload: dict[str, dict] = {}
    if not predict_df.empty:
        for user_id in sorted(predict_df["user_id"].dropna().unique().tolist()):
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
            users_payload[str(int(user_id))] = {
                "byGameType": by_game,
                "bestGameType": "other",
                "bestWinProbability": 50.0,
            }
    return {"generatedAt": now_iso(), "users": users_payload}


def _safe_prob(model, x: pd.DataFrame) -> float:
    if hasattr(model, "predict_proba"):
        return float(model.predict_proba(x)[0, 1])
    if hasattr(model, "decision_function"):
        score = float(model.decision_function(x)[0])
        return float(1.0 / (1.0 + np.exp(-score)))
    return float(model.predict(x)[0])


def _blend_probability(
    model_prob: float,
    game_baseline: float,
    personal_rate: float,
    prev_game_matches: int,
    prev_matches: int,
) -> float:
    personal_weight = clamp(prev_game_matches / 12.0, 0.0, 1.0)
    personal_blend = personal_weight * personal_rate + (1.0 - personal_weight) * game_baseline
    model_weight = 0.15 + 0.7 * clamp(prev_game_matches / 20.0, 0.0, 1.0) + 0.15 * clamp(prev_matches / 30.0, 0.0, 1.0)
    model_weight = clamp(model_weight, 0.15, 0.9)
    return clamp(model_weight * model_prob + (1.0 - model_weight) * personal_blend, 0.01, 0.99)


def main() -> None:
    args = parse_args()
    settings = load_settings(args.output_dir)
    output_dir: Path = settings.output_dir

    train_path = output_dir / "train.csv"
    predict_path = output_dir / "predict.csv"

    train_df = pd.read_csv(train_path) if train_path.exists() and train_path.stat().st_size > 0 else pd.DataFrame()
    predict_df = pd.read_csv(predict_path) if predict_path.exists() and predict_path.stat().st_size > 0 else pd.DataFrame()

    if args.user_id is not None:
        train_df = train_df[train_df.get("user_id") == args.user_id]
        predict_df = predict_df[predict_df.get("user_id") == args.user_id]
    if args.limit_users and not predict_df.empty:
        allowed = predict_df["user_id"].drop_duplicates().head(args.limit_users)
        predict_df = predict_df[predict_df["user_id"].isin(allowed)]
        if not train_df.empty:
            train_df = train_df[train_df["user_id"].isin(allowed)]

    features = [
        "prev_matches",
        "prev_win_rate",
        "prev_draw_rate",
        "prev_loss_rate",
        "prev_avg_points",
        "prev_form5_score",
        "prev_form10_score",
        "prev_recent_win_streak",
        "prev_recent_loss_streak",
        "prev_game_matches",
        "prev_game_win_rate",
        "prev_game_draw_rate",
        "prev_game_loss_rate",
        "prev_game_avg_points",
        "prev_game_form5_score",
        "is_squad",
        "game_fps",
        "game_sports",
        "game_battle_royale",
        "game_mind",
        "game_other",
    ]

    base_info = {
        "generatedAt": now_iso(),
        "features": features,
        "trainSamples": int(len(train_df)),
        "predictSamples": int(len(predict_df)),
    }

    if predict_df.empty:
        predictions = build_empty_predictions(predict_df)
        status = "no_data" if train_df.empty else "no_predict_rows"
        reason = "Both train.csv and predict.csv are empty" if train_df.empty else "predict.csv is empty; no users to score"
        model_info = {
            **base_info,
            "status": status,
            "reason": reason,
        }
        to_json_file(output_dir / "predictions.json", predictions)
        to_json_file(output_dir / "model_info.json", model_info)
        print("No predict rows. Wrote empty predictions.json and model_info.json")
        return

    for col in features + ["user_id", "game_key", "sample_size"]:
        if col not in predict_df.columns:
            predict_df[col] = 0

    for col in features + ["label_win"]:
        if col not in train_df.columns:
            train_df[col] = 0

    global_baseline = float(train_df["label_win"].mean()) if not train_df.empty else 0.5
    global_baseline = clamp(global_baseline if not np.isnan(global_baseline) else 0.5, 0.01, 0.99)

    game_baselines = {g: global_baseline for g in GAME_KEYS}
    if not train_df.empty and "game_key" in train_df.columns:
        for g in GAME_KEYS:
            sub = train_df[train_df["game_key"] == g]
            if not sub.empty:
                game_baselines[g] = clamp(float(sub["label_win"].mean()), 0.01, 0.99)

    status = "fallback"
    reason = None
    selected_model_name = None
    candidate_scores: dict[str, float] = {}
    cv_metric = "balanced_accuracy"
    cv_folds = 0
    train_accuracy = None
    positive_rate = None

    trained_model = None

    if train_df.empty and predict_df.empty:
        status = "no_data"
        reason = "Both training and prediction datasets are empty"
    elif len(train_df) < 24:
        reason = "Insufficient train rows (<24)"
    elif train_df["label_win"].nunique() < 2:
        reason = "Training labels contain a single class"
    else:
        X = train_df[features].fillna(0.0)
        y = train_df["label_win"].astype(int)

        min_class = int(y.value_counts().min()) if y.nunique() > 1 else 0
        cv_folds = max(2, min(5, min_class)) if min_class >= 2 else 0

        if cv_folds < 2:
            reason = "Not enough samples per class for CV"
        else:
            candidates = {
                "logistic_regression": Pipeline(
                    [
                        ("scaler", StandardScaler()),
                        ("model", LogisticRegression(max_iter=1000, class_weight="balanced", random_state=settings.random_state)),
                    ]
                ),
                "random_forest": RandomForestClassifier(
                    n_estimators=300,
                    class_weight="balanced_subsample",
                    random_state=settings.random_state,
                    n_jobs=-1,
                ),
                "hist_gradient_boosting": HistGradientBoostingClassifier(random_state=settings.random_state),
            }

            best_score = -np.inf
            best_name = None
            best_model = None

            cv = StratifiedKFold(n_splits=cv_folds, shuffle=True, random_state=settings.random_state)
            for name, model in candidates.items():
                try:
                    scores = cross_val_score(model, X, y, cv=cv, scoring="balanced_accuracy", error_score=np.nan)
                    mean_score = float(np.nanmean(scores)) if len(scores) else np.nan
                    if np.isnan(mean_score):
                        continue
                    candidate_scores[name] = round(mean_score, 6)
                    if mean_score > best_score:
                        best_score = mean_score
                        best_name = name
                        best_model = model
                except Exception:
                    continue

            if best_model is None:
                reason = "All candidate models failed during CV"
            else:
                best_model.fit(X, y)
                trained_model = best_model
                selected_model_name = best_name
                train_preds = best_model.predict(X)
                train_accuracy = float(balanced_accuracy_score(y, train_preds))
                positive_rate = float(y.mean())
                status = "trained"
                reason = None

    users_payload: dict[str, dict] = {}

    for user_id, group in predict_df.groupby("user_id", sort=False):
        by_game = {}
        for _, row in group.iterrows():
            game_key = row.get("game_key", "other")
            if game_key not in GAME_KEYS:
                continue

            x = pd.DataFrame([row[features].fillna(0.0)])
            prev_matches = int(row.get("prev_matches", 0))
            prev_game_matches = int(row.get("prev_game_matches", row.get("sample_size", 0)))
            personal_rate = float(row.get("prev_game_win_rate", 0.0)) if prev_game_matches > 0 else game_baselines.get(game_key, global_baseline)
            game_base = game_baselines.get(game_key, global_baseline)

            if trained_model is not None:
                model_prob = _safe_prob(trained_model, x)
                prob = _blend_probability(model_prob, game_base, personal_rate, prev_game_matches, prev_matches)
                model_name = selected_model_name or "trained_model"
            else:
                # Baseline fallback: blend game baseline and personal historical rate by game sample.
                w = clamp(prev_game_matches / 12.0, 0.0, 1.0)
                prob = clamp(w * personal_rate + (1.0 - w) * game_base, 0.01, 0.99)
                model_name = "baseline_fallback"

            prob_pct = round(prob * 100.0, 1)
            by_game[game_key] = {
                "winProbability": prob_pct,
                "expectedResult": "W" if prob_pct >= 55.0 else "L",
                "confidence": confidence_from_samples(prev_game_matches),
                "samplesSeen": prev_matches,
                "gameSamples": prev_game_matches,
                "model": model_name,
                "gameType": game_key,
            }

        for g in GAME_KEYS:
            if g not in by_game:
                by_game[g] = {
                    "winProbability": round(game_baselines.get(g, global_baseline) * 100.0, 1),
                    "expectedResult": "L",
                    "confidence": "low",
                    "samplesSeen": 0,
                    "gameSamples": 0,
                    "model": "baseline_fallback",
                    "gameType": g,
                }

        best_game = max(by_game.items(), key=lambda kv: kv[1]["winProbability"])
        users_payload[str(int(user_id))] = {
            "byGameType": {k: by_game[k] for k in GAME_KEYS},
            "bestGameType": best_game[0],
            "bestWinProbability": best_game[1]["winProbability"],
        }

    predictions = {"generatedAt": now_iso(), "users": users_payload}

    model_info = {**base_info, "status": status}
    if reason:
        model_info["reason"] = reason
    if status == "trained":
        model_info.update(
            {
                "selectedModel": selected_model_name,
                "candidateScores": candidate_scores,
                "cvMetric": cv_metric,
                "cvFolds": cv_folds,
                "trainAccuracy": round(train_accuracy or 0.0, 6),
                "positiveRate": round(positive_rate or 0.0, 6),
            }
        )
    else:
        model_info.update(
            {
                "selectedModel": "baseline_fallback",
                "candidateScores": candidate_scores,
                "cvMetric": cv_metric,
                "cvFolds": cv_folds,
            }
        )

    to_json_file(output_dir / "predictions.json", predictions)
    to_json_file(output_dir / "model_info.json", model_info)

    print(f"Wrote predictions to {output_dir / 'predictions.json'}")
    print(f"Wrote model info to {output_dir / 'model_info.json'}")


if __name__ == "__main__":
    main()
