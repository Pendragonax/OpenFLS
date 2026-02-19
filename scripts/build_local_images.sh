#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BACKEND_IMAGE="ghcr.io/pendragonax/openfls-backend:local"
FRONTEND_IMAGE="ghcr.io/pendragonax/openfls-frontend:local"

echo "Building backend image: ${BACKEND_IMAGE}"
docker build \
  --target prod \
  -t "${BACKEND_IMAGE}" \
  "${ROOT_DIR}/backend"

echo "Building frontend image: ${FRONTEND_IMAGE}"
docker build \
  --target prod \
  -t "${FRONTEND_IMAGE}" \
  "${ROOT_DIR}/frontend"

echo "Done."
