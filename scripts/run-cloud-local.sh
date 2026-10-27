#!/usr/bin/env bash
set -euo pipefail
cp -n .env.example .env 2>/dev/null || true
./scripts/build-all.sh
docker compose up --build -d
echo "IBMS is available through the API gateway at http://localhost:${GATEWAY_PORT:-8080}"
