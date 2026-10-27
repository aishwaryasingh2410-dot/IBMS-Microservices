# IBMS — Integrated Banking Management System

Academic project demonstrating a four-service banking application, local container orchestration, and an AWS deployment design.

> **Status:** The verified runnable deployment is local Docker Compose. AWS is design-only; no AWS infrastructure has been provisioned. This is an academic demonstration, not production banking software.

## Repository layout

```text
.
├── auth-service/          # Registration, login, and user lookup
├── transaction-service/   # Accounts, deposits, withdrawals, transfers
├── notification-service/  # User notifications
├── loan-service/          # Loan applications
├── cloud/
│   ├── nginx/              # API gateway routing
│   ├── mysql/init/         # Local and RDS database initialization
│   └── terraform/          # AWS infrastructure design
├── scripts/                # Build, demo, and deployment helpers
├── sql/                    # Database, table, and sample-data scripts
├── docs/                   # Requirements, report, and presentation
├── docker-compose.yml
└── docker-compose.aws.yml
```

The repository root is the runnable project root, with one copy of each service and its deployment configuration.

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

Docker Compose runs the four Spring Boot services, Nginx, and MySQL. Transaction calls Auth to verify account owners; Loan calls Notification after a loan application. Each service owns a separate logical database.

## Run locally

### Prerequisites

- Docker Desktop with Docker Compose v2
- `curl` and Python 3

### Start and verify

Run these commands from the repository root:

```sh
docker compose up --build -d
./scripts/demo.sh
```

The demo checks `http://localhost:8080/health` and exercises one real API for each service through the gateway. A successful run ends with:

```text
RESULT: PASS - gateway health and all four service APIs are working
```

The demo writes records to the local MySQL volume. Stop the containers and keep the data:

```sh
docker compose down
```

To also delete the local database volume:

```sh
docker compose down -v
```

## Database scripts

The SQL scripts are ordered and safe to rerun:

```sh
mysql -u root -p < sql/create_database.sql
mysql -u root -p < sql/create_tables.sql
mysql -u root -p < sql/insert_data.sql
```

Compose initializes the four logical databases automatically on a new MySQL volume. Hibernate creates or updates the service tables at startup.

## Configuration

- `.env.example` contains local Compose settings.
- `.env.aws.example` contains placeholders for the AWS Compose design.
- Service URLs are configurable with `AUTH_SERVICE_URL` and `NOTIFICATION_SERVICE_URL`.

Copy the relevant example to `.env` or `.env.aws` and provide local values as needed. Real environment files and credentials must remain untracked.

## AWS design (not deployed)

The design uses EC2 running Docker Compose and Nginx on port 80, plus private RDS MySQL with four logical databases. Terraform defines EC2, RDS, its DB subnet group, and security groups. HTTP reaches EC2; RDS MySQL ingress is restricted to the EC2 security group.

1. **Provision:** From `cloud/terraform`, create an untracked `terraform.tfvars` based on `terraform.tfvars.example`; run `terraform init -backend=false`, `terraform validate`, review `terraform plan`, and only apply after reviewing costs and changes.
2. **Configure:** Set the RDS endpoint and credentials in an untracked `.env.aws` based on `.env.aws.example`.
3. **Deploy:** Clone this repository to EC2 and run `./scripts/deploy-aws-ec2.sh`.
4. **Verify:** Check `http://<EC2_PUBLIC_IP>/health`, exercise routed APIs, and inspect Compose logs.
5. **Clean up:** When intentionally deleting the environment and database data, review the impact and run `terraform destroy` from `cloud/terraform`.

Terraform validation passed, but no plan/apply and no AWS deployment have been performed. AWS resources can incur charges. See [`cloud/AWS_DEPLOYMENT.md`](cloud/AWS_DEPLOYMENT.md) before considering provisioning.

## Submission documents

The requirements specification, project report, and combined presentation are in [`docs/`](docs/). The college-formatted folder/ZIP is kept separately from this clean GitHub repository layout.

## Kubernetes

Kubernetes is experimental and not part of this submission.

## Known limitations

Login does not issue access tokens, and API routes do not enforce authorization. This project is not suitable for real banking or production use.
