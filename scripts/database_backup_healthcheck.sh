#!/usr/bin/env bash

set -Eeuo pipefail

STATUS_FILE="${BACKUP_STATUS_DIR:-/backup/status}/latest.json"
BACKUP_MAX_AGE_HOURS="${BACKUP_MAX_AGE_HOURS:-7}"

case "$BACKUP_MAX_AGE_HOURS" in *[!0-9]*|'') exit 1;; esac
[ -r "$STATUS_FILE" ] || exit 1

status="$(<"$STATUS_FILE")"
printf '%s' "$status" | grep -q '"outcome":"success"' || exit 1
timestamp="$(printf '%s' "$status" | sed -n 's/.*"timestamp":"\([^"]*\)".*/\1/p')"
[ -n "$timestamp" ] || exit 1
timestamp_epoch="$(date -u -d "$timestamp" +%s 2>/dev/null)" || exit 1
now_epoch="$(date -u +%s)"
max_age_seconds="$(( BACKUP_MAX_AGE_HOURS * 3600 ))"
[ "$(( now_epoch - timestamp_epoch ))" -le "$max_age_seconds" ]
