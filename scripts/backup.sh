#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-./backups}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-blog}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-blog_pass}"
MYSQL_DATABASE="${MYSQL_DATABASE:-enterprise_blog}"

mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/backup-$(date +%Y%m%d%H%M%S).sql"
mysqldump -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" > "$FILE"
find "$BACKUP_DIR" -name 'backup-*.sql' -mtime +14 -delete
echo "$FILE"
