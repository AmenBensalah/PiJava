# User Performance AI Agent

Standalone Python 3.11+ package that reproduces Esportify profile AI behavior against the same MySQL database.
It extracts robust user match events, builds ML features, trains or falls back to baseline predictions, and builds profile-report payloads compatible with JavaFX/Symfony-style rendering.

## Project Structure

```text
user_ai_agent/
  README.md
  requirements.txt
  .env.example
  config.py
  sql/
    user_match_events.sql
  data/
    .gitkeep
  src/
    __init__.py
    utils.py
    db.py
    extract_events.py
    feature_engineering.py
    train_user_performance.py
    predict_user_performance.py
    build_profile_report.py
  scripts/
    run_all.ps1
    run_all.sh
```

## Prerequisites

- Python 3.11+
- MySQL database reachable from your machine
- Existing Esportify schema (with flexible naming handled by fallback detection)

## Installation

```bash
cd user_ai_agent
python -m venv .venv
# Linux/macOS
source .venv/bin/activate
# Windows PowerShell
# .\.venv\Scripts\Activate.ps1

pip install -r requirements.txt
```

## Environment Setup

```bash
cp .env.example .env
```

Set values in `.env`:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `OUTPUT_DIR` (default `./data`)
- `RANDOM_STATE` (default `42`)

## Run Commands

From `user_ai_agent/`:

```bash
python -m src.extract_events --output-dir ./data
python -m src.feature_engineering --output-dir ./data
python -m src.train_user_performance --output-dir ./data
```

One-command orchestration:

```bash
# Linux/macOS
bash scripts/run_all.sh ./data

# Windows PowerShell
./scripts/run_all.ps1 -OutputDir ./data
```

## Generate One User Profile Report

```bash
python -m src.build_profile_report --user-id 123 --output-dir ./data
```

This prints JSON to stdout and writes:

- `data/profile_report_user_123.json`

Generate all users at once:

```bash
python -m src.build_profile_report --all-users --output-dir ./data
```

This writes:

- `data/profile_reports/profile_report_user_<id>.json` for each user
- `data/profile_reports_all.json` (aggregate payload for frontend/JavaFX)

## Outputs

- `raw_events.csv`
- `train.csv`
- `predict.csv`
- `predictions.json`
- `model_info.json`

## Troubleshooting

### DB connection errors

- Verify `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
- Ensure MySQL user has read permissions on relevant tables.
- Confirm schema and table names are visible in the selected database.

### Empty datasets

- `raw_events.csv` empty: likely no resolved match/user table names or no eligible played/scored matches.
- `train.csv` empty: no historical events with valid result labels (`W/D/L`).
- `predict.csv` empty: no users found in extraction stage.

### Fallback mode

Fallback is expected when:

- train rows are fewer than 24
- labels contain a single class
- CV cannot run safely

In fallback, predictions still generate via blended baselines.

## `model_info.json` Status Values

- `trained`: model selected by CV and fitted successfully.
- `fallback`: baseline mode used due to data/model constraints (`reason` included).
- `no_predict_rows`: no prediction rows to score.
- `no_data`: both train and predict datasets are empty.

## Notes on Determinism and Idempotency

- All scripts are safe to run multiple times.
- Outputs are overwritten deterministically for the same input data.
- `RANDOM_STATE` controls model randomness.
