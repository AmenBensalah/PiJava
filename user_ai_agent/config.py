from __future__ import annotations

import os
import re
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv


@dataclass(frozen=True)
class Settings:
    db_host: str
    db_port: int
    db_name: str
    db_user: str
    db_password: str
    output_dir: Path
    random_state: int


def get_project_root() -> Path:
    return Path(__file__).resolve().parent


def load_settings(output_dir_override: str | None = None) -> Settings:
    """Load environment configuration with safe defaults."""
    load_dotenv(get_project_root() / ".env")
    pijava_url = os.getenv("PIJAVA_DB_URL", "")
    pijava_user = os.getenv("PIJAVA_DB_USER", "")
    pijava_password = os.getenv("PIJAVA_DB_PASSWORD", "")

    parsed_host = "localhost"
    parsed_port = 3306
    parsed_db = "esportify"
    if pijava_url:
        m = re.search(r"jdbc:mysql://([^:/?#]+)(?::(\d+))?/([^?]+)", pijava_url.strip())
        if m:
            parsed_host = m.group(1) or parsed_host
            parsed_port = int(m.group(2) or parsed_port)
            parsed_db = m.group(3) or parsed_db

    db_host = os.getenv("DB_HOST", parsed_host)
    db_port = int(os.getenv("DB_PORT", str(parsed_port)))
    db_name = os.getenv("DB_NAME", parsed_db)
    db_user = os.getenv("DB_USER", pijava_user or "root")
    db_password = os.getenv("DB_PASSWORD", pijava_password or "")
    random_state = int(os.getenv("RANDOM_STATE", "42"))

    output_raw = output_dir_override or os.getenv("OUTPUT_DIR", "./data")
    output_dir = (get_project_root() / output_raw).resolve() if not Path(output_raw).is_absolute() else Path(output_raw)
    output_dir.mkdir(parents=True, exist_ok=True)

    return Settings(
        db_host=db_host,
        db_port=db_port,
        db_name=db_name,
        db_user=db_user,
        db_password=db_password,
        output_dir=output_dir,
        random_state=random_state,
    )
