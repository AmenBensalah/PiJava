#!/usr/bin/env bash
set -euo pipefail

SQL_FILE="${1:-./database/esportify-2.sql}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-esportify}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
RECREATE_DB="${RECREATE_DB:-1}"

if [[ ! -f "$SQL_FILE" ]]; then
  echo "Fichier SQL introuvable: $SQL_FILE" >&2
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "mysql introuvable. Installe MySQL/MariaDB ou ajoute mysql au PATH." >&2
  exit 1
fi

AUTH_ARGS=(-h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER")
if [[ -n "$DB_PASSWORD" ]]; then
  AUTH_ARGS+=(--password="$DB_PASSWORD")
fi

if [[ "$RECREATE_DB" == "1" ]]; then
  mysql "${AUTH_ARGS[@]}" -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
else
  mysql "${AUTH_ARGS[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi
mysql "${AUTH_ARGS[@]}" "$DB_NAME" < "$SQL_FILE"

echo "Import termine. Base active: $DB_NAME"
