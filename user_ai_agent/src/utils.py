from __future__ import annotations

import json
import re
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable

import numpy as np
import pandas as pd


def now_iso() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat()


def normalize_text(value: Any) -> str:
    """Normalize free text for resilient alias matching."""
    if value is None:
        return ""
    text = str(value).strip().lower()
    text = re.sub(r"\s+", " ", text)
    return text


def normalize_game_type(value: Any) -> str:
    t = normalize_text(value).replace("-", " ").replace("_", " ")
    if not t:
        return "other"
    if "battle" in t and "royale" in t:
        return "battle_royale"
    if t in {"fps", "first person shooter", "shooter"}:
        return "fps"
    if t in {"sport", "sports", "football", "fifa", "nba", "tennis"}:
        return "sports"
    if "mind" in t or t in {"strategy", "puzzle", "chess"}:
        return "mind"
    return "other"


def safe_float(value: Any, default: float = 0.0) -> float:
    if value is None or (isinstance(value, float) and np.isnan(value)):
        return default
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def safe_int(value: Any, default: int = 0) -> int:
    if value is None:
        return default
    try:
        return int(float(value))
    except (TypeError, ValueError):
        return default


def to_json_file(path: Path, payload: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as fh:
        json.dump(payload, fh, ensure_ascii=False, indent=2)


def load_csv_or_empty(path: Path) -> pd.DataFrame:
    if not path.exists() or path.stat().st_size == 0:
        return pd.DataFrame()
    return pd.read_csv(path)


def normalize_result(value: Any) -> str | None:
    t = normalize_text(value)
    if not t:
        return None
    if t in {"w", "win", "won", "victory", "victoire"}:
        return "W"
    if t in {"d", "draw", "tie", "nul", "null"}:
        return "D"
    if t in {"l", "loss", "lost", "defeat", "défaite", "defaite"}:
        return "L"
    return None


def result_score(result: str | None) -> int:
    if result == "W":
        return 1
    if result == "L":
        return -1
    return 0


def form_score(results: Iterable[str], window: int) -> float:
    seq = [result_score(r) for r in list(results)[-window:]]
    if not seq:
        return 0.0
    return float(np.mean(seq))


def streak_from_end(results: list[str], target: str) -> int:
    count = 0
    for result in reversed(results):
        if result == target:
            count += 1
        else:
            break
    return count


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))
