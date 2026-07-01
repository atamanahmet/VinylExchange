#!/usr/bin/env bash
# =============================================================================
# Vault dev-mode setup for vinyl-exchange
#
# For LOCAL DEVELOPMENT ONLY. Vault runs with `server -dev`, which
# auto-initializes and auto-unseals on every boot with no durable storage.
# This is intentional, not a shortcut: Docker Desktop's WSL2 backend has a
# known filesystem bridging issue that breaks Vault's file storage barrier
# init on named volumes. Production uses HCP Vault Dedicated, which has its
# own managed storage and does not run this script at all.
#
# Safe to re-run: completed steps are skipped.
# Requires the vault CLI and a running Vault dev-mode server.
#
# Environment:
#   VAULT_ADDR        Vault API address (default: http://localhost:8200)
#   VAULT_DEV_TOKEN   The root token configured via VAULT_DEV_ROOT_TOKEN_ID
#                      on the Vault container (required)
# =============================================================================

set -euo pipefail

export VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"

READY_RETRIES=10
READY_DELAY_SECONDS=2

log() {
  echo "[vault-setup] $*"
}

on_interrupt() {
  echo "" >&2
  echo "[vault-setup] Interrupted, re-run this script to continue." >&2
  exit 130
}

trap on_interrupt SIGINT SIGTERM

require_vault() {
  if ! command -v vault >/dev/null 2>&1; then
    echo "Error: vault CLI not found in PATH" >&2
    exit 1
  fi
}

require_dev_token() {
  if [[ -z "${VAULT_DEV_TOKEN:-}" ]]; then
    echo "Error: VAULT_DEV_TOKEN is required. This must match VAULT_DEV_ROOT_TOKEN_ID" >&2
    echo "        set on the vault-local container." >&2
    exit 1
  fi
  export VAULT_TOKEN="$VAULT_DEV_TOKEN"
}

wait_for_vault() {
  local attempt=1
  while (( attempt <= READY_RETRIES )); do
    if curl -s "$VAULT_ADDR/v1/sys/health" >/dev/null 2>&1; then
      return 0
    fi
    log "Vault not reachable yet at $VAULT_ADDR (attempt $attempt/$READY_RETRIES), retrying..."
    sleep "$READY_DELAY_SECONDS"
    ((attempt++))
  done

  echo "Error: cannot reach Vault at $VAULT_ADDR after $READY_RETRIES attempts" >&2
  exit 1
}

enable_kv_v2() {
  if vault secrets list -format=json 2>/dev/null | jq -e 'has("secret/")' >/dev/null 2>&1; then
    log "KV v2 secrets engine already enabled at secret/"
    return
  fi

  log "Enabling KV v2 secrets engine at path secret/..."
  vault secrets enable -path=secret kv-v2
}

enable_approle() {
  if vault auth list -format=json 2>/dev/null | jq -e 'has("approle/")' >/dev/null 2>&1; then
    log "AppRole auth method already enabled"
    return
  fi

  log "Enabling AppRole auth method..."
  vault auth enable approle
}

write_policy() {
  log "Writing policy vinyl-exchange-policy..."
  vault policy write vinyl-exchange-policy - <<'EOF'
path "secret/data/vinyl-exchange/*" {
  capabilities = ["read"]
}
path "secret/data/vinyl-exchange" {
  capabilities = ["read"]
}
EOF
}

configure_approle() {
  log "Configuring AppRole vinyl-exchange..."
  vault write auth/approle/role/vinyl-exchange \
    token_policies="vinyl-exchange-policy" \
    token_ttl=1h \
    token_max_ttl=4h
}

write_credentials() {
  local role_id secret_id
  local target_file="${VAULT_CREDS_FILE:-.env.vault}"

  role_id="$(vault read -field=role_id auth/approle/role/vinyl-exchange/role-id)"
  secret_id="$(vault write -f -field=secret_id auth/approle/role/vinyl-exchange/secret-id)"

  cat > "$target_file" <<EOF
VAULT_ROLE_ID=$role_id
VAULT_SECRET_ID=$secret_id
EOF

  log "Wrote VAULT_ROLE_ID and VAULT_SECRET_ID to $target_file"
}

main() {
  require_vault
  require_dev_token
  wait_for_vault

  enable_kv_v2
  enable_approle
  write_policy
  configure_approle
  write_credentials

  log "Done."
}

main "$@"