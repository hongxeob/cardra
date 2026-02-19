#!/usr/bin/env bash
set -euo pipefail

# Minimal launcher for Oh-My-Codex (OMX).
# - Prefer PATH-installed `omx`
# - Fallback to common Homebrew global install path if PATH is different in shell context

if command -v omx >/dev/null 2>&1; then
  OMX_BIN="$(command -v omx)"
else
  OMX_BIN="/opt/homebrew/bin/omx"
fi

if [ ! -x "$OMX_BIN" ]; then
  echo "[OMX] omx binary not found."
  echo "Run: npm install -g oh-my-codex"
  echo "Then: ./scripts/omx.sh setup"
  exit 1
fi

exec "$OMX_BIN" "$@"
