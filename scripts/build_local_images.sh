#!/usr/bin/env bash
# Baut die lokalen Produktions-Images für Backend und Frontend.
set -euo pipefail

# Ermittelt den Projektstamm unabhängig vom aktuellen Arbeitsverzeichnis.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BACKEND_IMAGE="ghcr.io/pendragonax/openfls-backend:local"
FRONTEND_IMAGE="ghcr.io/pendragonax/openfls-frontend:local"

# Erstellt das Backend-Image aus dem Produktions-Build-Ziel.
echo "Building backend image: ${BACKEND_IMAGE}"
docker build \
  --target prod \
  -t "${BACKEND_IMAGE}" \
  "${ROOT_DIR}/backend"

# Erstellt das Frontend-Image aus dem Produktions-Build-Ziel.
echo "Building frontend image: ${FRONTEND_IMAGE}"
docker build \
  --target prod \
  -t "${FRONTEND_IMAGE}" \
  "${ROOT_DIR}/frontend"

echo "Done."
