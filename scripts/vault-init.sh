#!/usr/bin/env bash
# =============================================================================
# Vault secret seeding for vinyl-exchange
#
# Run ONCE immediately after vault-setup.sh. Delete this script after it succeeds.
# Requires the vault CLI, VAULT_TOKEN, and all secret source env vars below.
#
# Environment:
#   VAULT_ADDR  Vault API address (default: http://localhost:8200)
#   VAULT_TOKEN Token with write access to secret/vinyl-exchange/*
# =============================================================================

set -euo pipefail

export VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"

: "${VAULT_TOKEN:?VAULT_TOKEN is required}"

: "${AES_ENCRYPTION_KEY:?AES_ENCRYPTION_KEY is required}"
: "${ADMIN_TEST_EMAIL:?ADMIN_TEST_EMAIL is required}"
: "${ADMIN_TEST_PASSWORD:?ADMIN_TEST_PASSWORD is required}"
: "${DB_USER:?DB_USER is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
: "${JWT_SECRET:?JWT_SECRET is required}"
: "${IYZICO_API_KEY:?IYZICO_API_KEY is required}"
: "${IYZICO_SECRET_KEY:?IYZICO_SECRET_KEY is required}"
: "${IYZICO_BASE_URL:?IYZICO_BASE_URL is required}"
: "${IYZICO_CALLBACK_URL:?IYZICO_CALLBACK_URL is required}"
: "${CLOUDINARY_URL:?CLOUDINARY_URL is required}"
: "${OPENSEARCH_PASSWORD:?OPENSEARCH_PASSWORD is required}"

log() {
  echo "[vault-init] $*"
}

require_vault() {
  if ! command -v vault >/dev/null 2>&1; then
    echo "Error: vault CLI not found in PATH" >&2
    exit 1
  fi
}

seed_shared_secrets() {
  log "Writing secret/vinyl-exchange..."
  vault kv put secret/vinyl-exchange \
    aes.encryption.key="$AES_ENCRYPTION_KEY" \
    admin.test.email="$ADMIN_TEST_EMAIL" \
    admin.test.password="$ADMIN_TEST_PASSWORD"
}

seed_profile_secrets() {
  local profile="$1"
  log "Writing secret/vinyl-exchange/${profile}..."
  vault kv put "secret/vinyl-exchange/${profile}" \
    spring.datasource.username="$DB_USER" \
    spring.datasource.password="$DB_PASSWORD" \
    jwt.secret="$JWT_SECRET" \
    payment.iyzico.api-key="$IYZICO_API_KEY" \
    payment.iyzico.secret-key="$IYZICO_SECRET_KEY" \
    payment.iyzico.base-url="$IYZICO_BASE_URL" \
    payment.iyzico.callback-url="$IYZICO_CALLBACK_URL" \
    cloudinary.url="$CLOUDINARY_URL" \
    opensearch.password="$OPENSEARCH_PASSWORD"
}

main() {
  require_vault

  if ! vault status >/dev/null 2>&1; then
    echo "Error: cannot reach Vault at $VAULT_ADDR" >&2
    exit 1
  fi

  seed_shared_secrets
  seed_profile_secrets dev
  seed_profile_secrets prod

  echo "Vault seeded successfully"
}

main "$@"
