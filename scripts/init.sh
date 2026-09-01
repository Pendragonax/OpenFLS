#!/usr/bin/env bash
# Interaktiver Einstiegspunkt fuer die lokale Erstinitialisierung: legt die
# Standard-Secrets und das RSA-Schluesselpaar fuer die Token-Signatur an.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Welcome to OpenFLS!"
echo "This script creates the default secrets and the JWT signing keys."
echo -n "To proceed type 'go': "
read -r input

if [ "$input" = "go" ]; then
  "${ROOT_DIR}/scripts/database_create_secrets.sh"
  "${ROOT_DIR}/backend/scripts/backend_generate_rsa_keys.sh"
  echo
  echo "Done. Review the placeholder values in ${ROOT_DIR}/secrets/ before going to production."
else
  echo -e "\e[31maborted\e[0m"
  exit 1
fi
