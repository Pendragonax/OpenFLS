#!/usr/bin/env bash

set -Eeuo pipefail

BACKUP_INTERVAL_SECONDS="${BACKUP_INTERVAL_SECONDS:-21600}"
BACKUP_RETRY_INTERVAL_SECONDS="${BACKUP_RETRY_INTERVAL_SECONDS:-300}"

case "$BACKUP_INTERVAL_SECONDS" in *[!0-9]*|'') echo 'BACKUP_INTERVAL_SECONDS must be a positive integer' >&2; exit 2;; esac
case "$BACKUP_RETRY_INTERVAL_SECONDS" in *[!0-9]*|'') echo 'BACKUP_RETRY_INTERVAL_SECONDS must be a positive integer' >&2; exit 2;; esac

log() {
  printf '{"timestamp":"%s","level":"%s","event_name":"%s","outcome":"%s","service":"openfls-backup","message":"%s"}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$1" "$2" "$3" "$4"
}

while true; do
  if /usr/local/bin/openfls-backup-job; then
    log INFO backup.scheduler.wait success 'Backup finished; waiting for next scheduled run'
    sleep "$BACKUP_INTERVAL_SECONDS"
  else
    log ERROR backup.scheduler.retry failure 'Backup failed; waiting before retry'
    sleep "$BACKUP_RETRY_INTERVAL_SECONDS"
  fi
done
