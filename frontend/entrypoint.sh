#!/bin/bash

# Only build node_modules if they do not exist or if package-lock changed
mkdir -p /cache
if [ ! -d node_modules ] || (! sha1sum -c /cache/lock >/dev/null 2>&1); then
  echo "cache is invalid, rebuilding..."

  echo "rebuild dependencies..."
  [ -f package-lock.json ] && npm ci --include=dev || npm install --include=dev

  echo "creating hash for cache validation..."
  sha1sum package-lock.json > /cache/lock
else
  echo "cache is valid, continuing..."
fi

ng serve --host 0.0.0.0 --port 4200 --disable-host-check
