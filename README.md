# IBMS — Integrated Banking Management System

Academic project demonstrating a four-service banking application, local container orchestration, and an AWS deployment design.

> **Submission status:** The verified runnable deployment is local Docker Compose. AWS is design-only: no AWS infrastructure has been provisioned. This is an academic demonstration, not production banking software.

## Start the local demo

### Prerequisites

- Docker Desktop with Docker Compose v2
- `curl` and Python 3

### Run

From the repository root:

```sh
cd A004_Project/Project/src
docker compose up --build -d
./scripts/demo.sh
```

The demo checks the Nginx gateway at `http://localhost:8080/health`, then exercises one real API through the gateway for Auth, Transaction, Notification, and Loan. It prints a PASS/FAIL result for each check. Demo records are stored in the local MySQL volume.

Stop the containers and keep the data:

```sh
docker compose down
```

To delete the local database volume as well:

```sh
docker compose down -v
```

## Architecture

```text
Client
  |
  v
Nginx API gateway :8080
  +-- Auth         :8081  -> auth_db
  +-- Transaction  :8082  -> transaction_db
  +-- Notification :8083  -> notification_db
  +-- Loan         :8084  -> loan_db
                         Local MySQL
```

Transaction verifies account owners through Auth; Loan sends notifications through Notification. Each service owns its logical database. The local stack runs four Spring Boot services, MySQL, and Nginx in Docker Compose.

## Submission package

The complete college deliverable is under [`A004_Project/`](A004_Project/Project/README.md):

- `A004_Project/Project/src/` — all four services, Compose files, Nginx configuration, environment examples, cloud configuration, Terraform, and scripts.
- `A004_Project/Project/sql/` — database creation, table DDL, and sample data.
- `A004_Project/Project/README.md` — setup and run instructions.
- `A004_Project/Documents/` — requirements specification, project report, and combined presentation.

The report includes the available Swagger screenshots. Duplicate source trees, old separate presentations, and editor-specific project files are intentionally not kept at the repository root.

## Cloud deployment status

The AWS design uses EC2 for Docker Compose and Nginx on port 80, plus private RDS MySQL with four logical databases. Terraform defines EC2, RDS, the RDS DB subnet group, and security groups. HTTP reaches EC2; RDS MySQL ingress is restricted to the EC2 security group.

Terraform configuration validation passed. It has **not** been planned or applied, and the AWS deployment has not been run. AWS resources may incur charges. See [`A004_Project/Project/src/cloud/AWS_DEPLOYMENT.md`](A004_Project/Project/src/cloud/AWS_DEPLOYMENT.md) before considering cloud provisioning. Do not commit credentials or real `.env` files.

Kubernetes is experimental and excluded from the submission.

## Limitations

Login does not issue access tokens, and API routes do not enforce authorization. The services demonstrate academic workflows and are not suitable for real banking or production use.
