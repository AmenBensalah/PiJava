param(
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$RootDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $RootDir

if ($OutputDir -ne "") {
    python -m src.extract_events --output-dir $OutputDir
    python -m src.feature_engineering --output-dir $OutputDir
    python -m src.train_user_performance --output-dir $OutputDir
    $OutPath = $OutputDir
} else {
    python -m src.extract_events
    python -m src.feature_engineering
    python -m src.train_user_performance
    $OutPath = if ($env:OUTPUT_DIR) { $env:OUTPUT_DIR } else { "./data" }
}

Write-Host "Pipeline completed."
Write-Host "raw_events.csv: $OutPath/raw_events.csv"
Write-Host "train.csv: $OutPath/train.csv"
Write-Host "predict.csv: $OutPath/predict.csv"
Write-Host "predictions.json: $OutPath/predictions.json"
Write-Host "model_info.json: $OutPath/model_info.json"
