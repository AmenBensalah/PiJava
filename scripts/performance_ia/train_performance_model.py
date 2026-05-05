from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Tuple

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


FEATURE_COLUMNS = [
    "team_region",
    "team_level",
    "completed_tasks",
    "total_tasks",
    "delayed_tasks",
    "blocked_tasks",
    "active_members",
    "open_recruitments",
    "avg_workload",
    "team_morale",
]

TARGET_COLUMN = "performance_label"


def load_dataset(csv_path: Path) -> pd.DataFrame:
    if not csv_path.exists():
        raise FileNotFoundError(f"Dataset introuvable: {csv_path}")

    frame = pd.read_csv(csv_path)
    missing = [column for column in FEATURE_COLUMNS + [TARGET_COLUMN] if column not in frame.columns]
    if missing:
        raise ValueError(f"Colonnes manquantes dans le dataset: {', '.join(missing)}")
    return frame


def build_pipeline() -> Pipeline:
    categorical_features = ["team_region", "team_level", "team_morale"]
    numeric_features = [
        "completed_tasks",
        "total_tasks",
        "delayed_tasks",
        "blocked_tasks",
        "active_members",
        "open_recruitments",
        "avg_workload",
    ]

    preprocessor = ColumnTransformer(
        transformers=[
            (
                "categorical",
                Pipeline(
                    steps=[
                        ("imputer", SimpleImputer(strategy="most_frequent")),
                        ("encoder", OneHotEncoder(handle_unknown="ignore")),
                    ]
                ),
                categorical_features,
            ),
            (
                "numeric",
                Pipeline(
                    steps=[
                        ("imputer", SimpleImputer(strategy="median")),
                        ("scaler", StandardScaler()),
                    ]
                ),
                numeric_features,
            ),
        ]
    )

    classifier = RandomForestClassifier(
        n_estimators=250,
        max_depth=10,
        min_samples_leaf=2,
        random_state=42,
        class_weight="balanced",
    )

    return Pipeline(
        steps=[
            ("preprocessor", preprocessor),
            ("classifier", classifier),
        ]
    )


def split_dataset(frame: pd.DataFrame) -> Tuple[pd.DataFrame, pd.DataFrame, pd.Series, pd.Series]:
    x = frame[FEATURE_COLUMNS]
    y = frame[TARGET_COLUMN]
    return train_test_split(
        x,
        y,
        test_size=0.25,
        random_state=42,
        stratify=y,
    )


def train_model(csv_path: Path, output_dir: Path) -> None:
    frame = load_dataset(csv_path)
    x_train, x_test, y_train, y_test = split_dataset(frame)

    pipeline = build_pipeline()
    pipeline.fit(x_train, y_train)

    predictions = pipeline.predict(x_test)
    report = classification_report(y_test, predictions, digits=3)
    accuracy = float((predictions == y_test).mean())

    output_dir.mkdir(parents=True, exist_ok=True)
    model_path = output_dir / "performance_model.joblib"
    metadata_path = output_dir / "performance_model_metadata.json"

    joblib.dump(pipeline, model_path)
    metadata = {
        "model_type": "RandomForestClassifier",
        "feature_columns": FEATURE_COLUMNS,
        "target_column": TARGET_COLUMN,
        "classes": sorted(frame[TARGET_COLUMN].unique().tolist()),
        "train_rows": int(len(x_train)),
        "test_rows": int(len(x_test)),
        "accuracy": round(accuracy, 4),
    }
    metadata_path.write_text(json.dumps(metadata, indent=2), encoding="utf-8")

    print("Modele performance entraine avec succes.")
    print(f"Dataset: {csv_path}")
    print(f"Modele sauvegarde: {model_path}")
    print(f"Metadata sauvegardees: {metadata_path}")
    print()
    print("Rapport de classification:")
    print(report)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Entrainement du modele Performance IA Esportify")
    parser.add_argument(
        "--dataset",
        default="scripts/performance_ia/data/performance_training_dataset.csv",
        help="Chemin vers le CSV d'entrainement",
    )
    parser.add_argument(
        "--output-dir",
        default="scripts/performance_ia/output",
        help="Dossier de sortie pour le modele entraine",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    train_model(Path(args.dataset), Path(args.output_dir))


if __name__ == "__main__":
    main()
