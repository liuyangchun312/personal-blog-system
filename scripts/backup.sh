#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-./backups}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-blog}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-blog_pass}"
MYSQL_DATABASE="${MYSQL_DATABASE:-enterprise_blog}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
COMPOSE_PROJECT="${COMPOSE_PROJECT:-enterprise-blog}"

mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/backup-$(date +%Y%m%d%H%M%S).sql.gz"

if command -v mysqldump >/dev/null 2>&1; then
  mysqldump -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" | gzip > "$FILE"
else
  docker compose -p "$COMPOSE_PROJECT" exec -T mysql sh -c "mysqldump -u\"\$MYSQL_USER\" -p\"\$MYSQL_PASSWORD\" \"\$MYSQL_DATABASE\"" | gzip > "$FILE"
fi

find "$BACKUP_DIR" -name 'backup-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete
echo "$FILE"
