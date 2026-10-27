# IBMS — Integrated Banking Management System

## Overview

IBMS is an academic banking application built as four independently deployable Spring Boot services. It demonstrates microservices, REST APIs, Docker Compose, an Nginx API gateway, and environment-based configuration.

| Service | Responsibility | Logical database |
|---|---|---|
| Auth | Registration, login, and user lookup | `auth_db` |
| Transaction | Accounts, deposits, withdrawals, transfers | `transaction_db` |
| Notification | User notifications | `notification_db` |
| Loan | Loan applications | `loan_db` |

Locally, Docker Compose runs the four services, Nginx gateway, and MySQL. The gateway is `http://localhost:8080`; health check is `/health`. Transaction calls Auth to verify account ownership, and Loan calls Notification after a loan application.

AWS is a **design-only** deployment path in this submission. No AWS resources have been created or applied.

## Folder contents

- `src/auth-service/`, `src/transaction-service/`, `src/notification-service/`, `src/loan-service/`: complete service source and Maven projects.
- `src/cloud/nginx/nginx.conf`: gateway routing configuration.
- `src/cloud/mysql/init/`: local MySQL initialization script.
- `src/cloud/terraform/` and `src/cloud/AWS_DEPLOYMENT.md`: AWS infrastructure definition and deployment guide.
- `src/docker-compose.yml`, `src/docker-compose.aws.yml`: local and AWS-oriented Compose configurations.
- `src/.env.example`, `src/.env.aws.example`: configuration templates containing placeholders only.
- `src/scripts/demo.sh`: local demonstration and API smoke test.
- `sql/`: portable database, table, and sample-data scripts.

Build outputs and local environment files are excluded from this package.

## Prerequisites

- Docker Desktop with Docker Compose v2
- `curl` and Python 3 (used by `scripts/demo.sh`)
- For manual Java/Maven runs only: Java 21 (the service Maven wrappers are included)

## Database setup

The three SQL scripts are ordered and safe to rerun:

```sh
cd sql
mysql -u root -p < create_database.sql
mysql -u root -p < create_tables.sql
mysql -u root -p < insert_data.sql
```

They create four logical databases, all five service tables, and one small demo user/account/transaction/notification/loan record. The demo user's documented credentials are `demo@example.com` / `DemoPassword123!`; the SQL stores a BCrypt hash, not the plaintext password.

The local Compose MySQL container automatically creates the four databases when its data volume is initialized for the first time. Hibernate creates or updates the service tables at startup. The explicit `create_tables.sql` is provided for manual database setup and schema review.

## Local Docker Compose

Run all commands from `Project/src`:

```sh
cp .env.example .env
# Optional: set matching MYSQL_ROOT_PASSWORD and DB_PASSWORD values in .env.
docker compose up --build -d
curl -i http://localhost:8080/health
./scripts/demo.sh
```

The demo script brings up the stack, waits for all four APIs, checks gateway health, and makes a real API request through the gateway to Auth, Transaction, Notification, and Loan. It prints PASS/FAIL for each check. Successful completion reports:

```text
RESULT: PASS - gateway health and all four service APIs are working
```

The demo writes test data to the local MySQL volume and does not delete it. Stop the services while retaining their data:

```sh
docker compose down
```

To delete the local database volume and its contents as well:

```sh
docker compose down -v
```

## Database and service configuration

Local Compose settings are in `.env.example`. The database host defaults to the Compose service name `mysql`; the four logical database names default to their service-specific names. For the AWS Compose design, `.env.aws.example` provides placeholders for `MYSQL_RDS_HOST`, `DB_USERNAME`, and `DB_PASSWORD`. Never commit real `.env` or credential files.

Service-to-service URLs are configurable as `AUTH_SERVICE_URL` and `NOTIFICATION_SERVICE_URL`; the Compose defaults use the internal service names on the bridge network.

## AWS design steps (not deployed)

The AWS design runs Docker Compose and Nginx on EC2 port 80 and uses private RDS MySQL for the four logical databases. Terraform defines EC2, RDS, its DB subnet group, and security groups. HTTP reaches EC2 on port 80; MySQL ingress is restricted to the EC2 security group. AWS may incur charges.

1. **Provision:** From `src/cloud/terraform`, create a local `terraform.tfvars` using `terraform.tfvars.example`, then run `terraform init -backend=false`, `terraform validate`, and review `terraform plan`. Only proceed with `terraform apply` after reviewing the plan and costs.
2. **Configure:** On EC2, set RDS endpoint and credentials in an untracked `.env.aws` based on `src/.env.aws.example`. Do not store secrets in Git.
3. **Deploy:** Copy/clone this `src` tree to EC2 and run `./scripts/deploy-aws-ec2.sh`. The AWS Compose `db-init` container runs `cloud/mysql/init/01-create-databases.sql` against RDS before services start.
4. **Verify:** Check `http://<EC2_PUBLIC_IP>/health`, exercise routed APIs, and inspect containers/logs with `docker compose --env-file .env.aws -f docker-compose.aws.yml ps` and `logs`.
5. **Cleanup:** When intentionally deleting the cloud environment and RDS data, review the impact and run `terraform destroy` from `cloud/terraform`.

Terraform validation has passed. No Terraform plan/apply and no AWS deployment have been performed. See `src/cloud/AWS_DEPLOYMENT.md` for the detailed cloud design.

## Kubernetes

Kubernetes is experimental and not part of this submission. No Kubernetes resources are required to run this project.

## Known academic limitations

This project is a demonstration, not production banking software. Login does not issue access tokens, and there is no authorization layer protecting the APIs.
