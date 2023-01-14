#!/bin/bash

# Only build node_modules if they do not exist or if package-lock changed
if [ ! -d node_modules ] || (! sha1sum --check /cache/lock); then
  echo "cache is invalid, rebuilding..."

  echo "rebuild dependencies..."
  [ -f package-lock.json ] && npm ci || npm i

  echo "creating hash for cache validation..."
  sha1sum package-lock.json > /cache/lock
else
  echo "cache is valid, continuing..."
fi

ng serve --host 0.0.0.0 --port 4200 --disable-host-check
