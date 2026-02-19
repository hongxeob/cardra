#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
git config core.hooksPath .githooks
echo "git hooks configured to use .githooks"
