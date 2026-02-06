#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEST_DIR="${BACKEND_DIR}/src/main/resources"

TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

echo -e "\e[36m[INFO] generate temporary keypair\e[0m"
openssl genrsa -out "${TMP_DIR}/keypair.pem" 2048

echo -e "\e[36m[INFO] generate temporary public-key\e[0m"
openssl rsa -in "${TMP_DIR}/keypair.pem" -pubout -out "${TMP_DIR}/public.key"

echo -e "\e[36m[INFO] generate temporary private-key\e[0m"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "${TMP_DIR}/keypair.pem" -out "${TMP_DIR}/private.key"

echo -e "\e[36m[INFO] copy private-key to ${DEST_DIR}/private.key\e[0m"
cp "${TMP_DIR}/private.key" "${DEST_DIR}/private.key"

echo -e "\e[36m[INFO] copy public-key to ${DEST_DIR}/public.key\e[0m"
cp "${TMP_DIR}/public.key" "${DEST_DIR}/public.key"

echo -e "\e[36m[INFO] generated backend keys\e[0m"
