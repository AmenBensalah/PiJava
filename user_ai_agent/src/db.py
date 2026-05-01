from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable

import pandas as pd
from sqlalchemy import create_engine, inspect, text
from sqlalchemy.engine import Engine

from config import Settings


@dataclass(frozen=True)
class ResolvedTable:
    name: str | None
    columns: list[dict]


def build_engine(settings: Settings) -> Engine:
    url = (
        f"mysql+pymysql://{settings.db_user}:{settings.db_password}@"
        f"{settings.db_host}:{settings.db_port}/{settings.db_name}?charset=utf8mb4"
    )
    return create_engine(url, future=True, pool_pre_ping=True)


def list_tables(engine: Engine) -> list[str]:
    return inspect(engine).get_table_names()


def resolve_table(engine: Engine, candidates: Iterable[str]) -> ResolvedTable:
    tables = list_tables(engine)
    lower_map = {t.lower(): t for t in tables}
    for c in candidates:
        if c.lower() in lower_map:
            real_name = lower_map[c.lower()]
            return ResolvedTable(name=real_name, columns=inspect(engine).get_columns(real_name))
    return ResolvedTable(name=None, columns=[])


def get_column_names(engine: Engine, table_name: str) -> list[str]:
    return [col["name"] for col in inspect(engine).get_columns(table_name)]


def pick_column(columns: list[str], candidates: Iterable[str]) -> str | None:
    lower = {c.lower(): c for c in columns}
    for cand in candidates:
        if cand.lower() in lower:
            return lower[cand.lower()]
    return None


def read_table(engine: Engine, table_name: str, columns: list[str] | None = None) -> pd.DataFrame:
    col_sql = "*" if not columns else ", ".join(f"`{c}`" for c in columns)
    sql = text(f"SELECT {col_sql} FROM `{table_name}`")
    with engine.connect() as conn:
        return pd.read_sql(sql, conn)
