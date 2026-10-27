#!/usr/bin/env python3
"""Exercise the local Compose APIs through the gateway without external packages."""

import json
import sys
import time
import urllib.error
import urllib.request
import uuid
from decimal import Decimal


BASE_URL = sys.argv[1].rstrip("/") if len(sys.argv) > 1 else "http://localhost:8080"


def request(method, path, body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(
        BASE_URL + path,
        data=data,
        method=method,
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as response:
            raw = response.read()
            return response.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        raw = error.read()
        try:
            payload = json.loads(raw)
        except (json.JSONDecodeError, UnicodeDecodeError):
            payload = raw.decode(errors="replace")
        return error.code, payload


def expect(method, path, body, expected_status):
    status, payload = request(method, path, body)
    if status != expected_status:
        raise AssertionError(
            f"{method} {path}: expected HTTP {expected_status}, got {status}: {payload}"
        )
    return payload


def ready():
    for _ in range(60):
        try:
            status, _ = request("GET", "/auth/users/1")
            if status in (200, 404):
                return
        except (OSError, TimeoutError):
            pass
        time.sleep(3)
    raise RuntimeError(f"Auth did not become ready through {BASE_URL}")


def main():
    ready()
    profile = expect("GET", "/auth/users/1", None, 200)
    assert "password" not in profile, "Auth response must never include a password field"
    print("PASS auth user lookup excludes password")

    email = f"compose-e2e-{uuid.uuid4().hex}@example.test"
    register = {
        "name": "Compose E2E",
        "email": email,
        "password": "ComposePass123!",
        "phone": "555-0199",
    }
    user = expect("POST", "/auth/register", register, 201)
    assert "password" not in user
    user_id = user["id"]
    expect("POST", "/auth/register", register, 409)
    print("PASS registration, safe response, and duplicate rejection")

    login = expect(
        "POST",
        "/auth/login",
        {"email": email.upper(), "password": register["password"]},
        200,
    )
    assert login["id"] == user_id and "password" not in login
    expect(
        "POST",
        "/auth/login",
        {"email": email, "password": "IncorrectPass123!"},
        401,
    )
    expect(
        "POST",
        "/auth/register",
        {"name": "", "email": "not-an-email", "password": "short"},
        400,
    )
    print("PASS normalized login, incorrect-password rejection, and input validation")

    first = expect("POST", "/accounts", {"userId": user_id}, 200)
    second = expect("POST", "/accounts", {"userId": user_id}, 200)
    first_id, second_id = first["id"], second["id"]
    assert Decimal(str(first["balance"])) == Decimal("0.00")
    expect("POST", "/accounts", {"userId": 999999999}, 404)
    print("PASS account creation verifies owner through Auth")

    deposit = expect(
        "POST",
        "/transactions/deposit",
        {"accountId": first_id, "amount": 100.00},
        200,
    )
    assert deposit["type"] == "DEPOSIT"
    withdrawal = expect(
        "POST",
        "/transactions/withdraw",
        {"accountId": first_id, "amount": 30.00},
        200,
    )
    assert withdrawal["type"] == "WITHDRAW"
    expect(
        "POST",
        "/transactions/withdraw",
        {"accountId": first_id, "amount": 100.00},
        422,
    )
    expect(
        "POST",
        "/transactions/deposit",
        {"accountId": first_id, "amount": 0},
        400,
    )
    transfer = expect(
        "POST",
        "/transactions/transfer",
        {
            "accountId": first_id,
            "destinationAccountId": second_id,
            "amount": 20.00,
        },
        200,
    )
    assert transfer["type"] == "TRANSFER"
    assert transfer["destinationAccountId"] == second_id
    expect(
        "POST",
        "/transactions/transfer",
        {
            "accountId": first_id,
            "destinationAccountId": first_id,
            "amount": 1.00,
        },
        400,
    )
    expect(
        "POST",
        "/transactions/transfer",
        {
            "accountId": first_id,
            "destinationAccountId": 999999999,
            "amount": 1.00,
        },
        404,
    )
    balance1 = expect("GET", f"/accounts/{first_id}", None, 200)
    balance2 = expect("GET", f"/accounts/{second_id}", None, 200)
    assert Decimal(str(balance1["balance"])) == Decimal("50.00"), balance1
    assert Decimal(str(balance2["balance"])) == Decimal("20.00"), balance2
    expect("GET", f"/transactions/{transfer['id']}", None, 200)
    expect("GET", "/accounts/999999999", None, 404)
    print("PASS deposit, withdrawal, transfer, exact balances, and failure cases")

    loan = expect(
        "POST",
        "/loans/apply",
        {"userId": user_id, "amount": 1000.00},
        200,
    )
    assert loan["status"] == "PENDING"
    notifications = expect("GET", f"/notifications/{user_id}", None, 200)
    assert any(item["type"] == "LOAN" for item in notifications), notifications
    print("PASS loan submission triggers notification through service communication")
    print("RESULT: Docker Compose end-to-end smoke tests passed")
    print("Note: test users, accounts, transactions, loan, and notification remain in MySQL.")


if __name__ == "__main__":
    main()
