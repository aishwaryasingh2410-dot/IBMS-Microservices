#!/usr/bin/env bash
set -euo pipefail
for service in auth-service transaction-service notification-service loan-service; do
  echo "Building $service..."
  (cd "$service" && ./mvnw -DskipTests package)
done
echo "All services built."
