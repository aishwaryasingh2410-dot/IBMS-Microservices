#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
base_url="${BASE_URL:-http://localhost:8080}"

echo "Starting the local IBMS stack..."
docker compose up --build -d

echo "Checking gateway health at ${base_url}/health..."
ready=false
for _ in $(seq 1 60); do
  if curl --silent --fail "$base_url/health"; then
    ready=true
    printf '\nPASS gateway health\n'
    break
  fi
  sleep 2
done
if [[ "$ready" != true ]]; then
  echo "FAIL gateway health: ${base_url}/health did not return HTTP 200" >&2
  exit 1
fi

IBMS_BASE_URL="$base_url" python3 - <<'PY'
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid

base_url = os.environ["IBMS_BASE_URL"].rstrip("/")
checks_passed = True


def request(method, path, body=None):
    data = None if body is None else json.dumps(body).encode()
    request = urllib.request.Request(
        base_url + path,
        data=data,
        method=method,
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            raw = response.read()
            return response.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        raw = error.read()
        try:
            payload = json.loads(raw)
        except (json.JSONDecodeError, UnicodeDecodeError):
            payload = raw.decode(errors="replace")
        return error.code, payload


for label, path in (
    ("auth", "/auth/users/1"),
    ("transaction", "/accounts/1"),
    ("notification", "/notifications/1"),
    ("loan", "/loans/1"),
):
    for _ in range(60):
        try:
            status, _ = request("GET", path)
            if status in (200, 404):
                break
        except (OSError, TimeoutError):
            pass
        time.sleep(2)
    else:
        print(f"FAIL {label} readiness: {path} did not respond through the gateway")
        sys.exit(1)


def check(label, method, path, body, expected_status):
    global checks_passed
    try:
        status, payload = request(method, path, body)
        if status != expected_status:
            checks_passed = False
            print(f"FAIL {label}: {method} {path} returned HTTP {status}: {payload}")
            return None
        print(
            f"PASS {label}: {method} {path} -> HTTP {status} "
            f"{json.dumps(payload, separators=(',', ':'))}"
        )
        return payload
    except (OSError, TimeoutError, json.JSONDecodeError) as error:
        checks_passed = False
        print(f"FAIL {label}: {method} {path}: {error}")
        return None


email = f"ibms-demo-{uuid.uuid4().hex}@example.test"
user = check(
    "auth service",
    "POST",
    "/auth/register",
    {
        "name": "IBMS Demo",
        "email": email,
        "password": "DemoPass123!",
        "phone": "555-0100",
    },
    201,
)
user_id = user.get("id", 1) if isinstance(user, dict) else 1

check(
    "transaction service",
    "POST",
    "/accounts",
    {"userId": user_id},
    200,
)
check(
    "notification service",
    "POST",
    "/notifications",
    {"userId": user_id, "message": "IBMS demo notification", "type": "DEMO"},
    200,
)
check(
    "loan service",
    "POST",
    "/loans/apply",
    {"userId": user_id, "amount": 1000.00},
    200,
)

if not checks_passed:
    print("RESULT: FAIL - one or more service checks failed")
    sys.exit(1)

print("RESULT: PASS - gateway health and all four service APIs are working")
print("Note: demo records remain in the local MySQL volume.")
PY
