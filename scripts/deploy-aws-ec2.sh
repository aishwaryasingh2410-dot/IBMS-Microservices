#!/usr/bin/env bash
set -euo pipefail

# Run this on the EC2 host after copying/cloning the repository.
# Expected variables are supplied through .env.aws.

if [ ! -f .env.aws ]; then
  echo "Missing .env.aws. Copy .env.aws.example to .env.aws and fill in RDS values."
  exit 1
fi

./scripts/build-all.sh
docker compose --env-file .env.aws -f docker-compose.aws.yml up --build -d

echo "IBMS cloud deployment started."
echo "Gateway: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<EC2_PUBLIC_IP>')"
