#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

OUTPUT_DIR_ARG="${1:-}"

if [[ -n "$OUTPUT_DIR_ARG" ]]; then
  python -m src.extract_events --output-dir "$OUTPUT_DIR_ARG"
  python -m src.feature_engineering --output-dir "$OUTPUT_DIR_ARG"
  python -m src.train_user_performance --output-dir "$OUTPUT_DIR_ARG"
  OUT_PATH="$OUTPUT_DIR_ARG"
else
  python -m src.extract_events
  python -m src.feature_engineering
  python -m src.train_user_performance
  OUT_PATH="${OUTPUT_DIR:-./data}"
fi

echo "Pipeline completed."
echo "raw_events.csv: ${OUT_PATH}/raw_events.csv"
echo "train.csv: ${OUT_PATH}/train.csv"
echo "predict.csv: ${OUT_PATH}/predict.csv"
echo "predictions.json: ${OUT_PATH}/predictions.json"
echo "model_info.json: ${OUT_PATH}/model_info.json"
