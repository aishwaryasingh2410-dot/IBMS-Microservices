# IBMS Cloud Computing Integration – AWS

This project now demonstrates both **Microservices Architecture** and **Cloud Computing** using the same banking application.

## Cloud topics demonstrated

| Cloud topic | IBMS implementation |
|---|---|
| IaaS | AWS EC2 hosts the application containers |
| Managed DB / PaaS-style service | AWS RDS for MySQL |
| Containerization | Docker images for all four Spring Boot services |
| Container orchestration | Docker Compose for the multi-service stack |
| Networking | Docker bridge network + AWS Security Groups |
| Reverse proxy / API gateway | Nginx container exposes one public HTTP entry point |
| Persistent storage | Docker volume locally; RDS in AWS |
| Configuration management | Environment variables / `.env` instead of hard-coded cloud settings |
| Infrastructure as Code | Terraform creates EC2, RDS, security groups and DB subnet group |
| Scalability foundation | Services are stateless at application level and can be replicated behind a load balancer later |

## Architecture

```text
                         INTERNET
                            |
                       AWS EC2 :80
                            |
                     Nginx API Gateway
                            |
       +--------------------+--------------------+
       |          |                 |             |
     Auth   Transaction        Notification      Loan
       |          |                 |             |
       +----------+-----------------+-------------+
                            |
                       AWS RDS MySQL
                 auth_db / transaction_db /
                 notification_db / loan_db
```

## Important distinction

The original project remains a **microservices project** because the application is split into independently deployable services with separate databases and OpenFeign communication.

The cloud layer is added without changing that business architecture:

**MSA = how the application is structured.**

**Cloud Computing = where/how the application is deployed, networked, stored, and managed.**

## Local cloud-style demonstration

1. Install Docker Desktop and Maven/Java 21.
2. Build all services:
   `./scripts/build-all.sh`
3. Start the stack:
   `docker compose up --build -d`
4. Test the gateway:
   `http://localhost:8080/health`
5. APIs are routed through the gateway, for example:
   - `POST /auth/register`
   - `POST /accounts`
   - `POST /transactions/deposit`
   - `POST /transactions/withdraw`
   - `POST /transactions/transfer`
   - `POST /notifications`
   - `POST /loans/apply`
6. Stop:
   `docker compose down`

## AWS deployment with Terraform

> AWS resources can incur charges. Review the AWS pricing/free-tier eligibility before applying Terraform.

1. Install AWS CLI, Terraform and Git.
2. Configure AWS credentials locally.
3. Go to `cloud/terraform`.
4. Copy `terraform.tfvars.example` to `terraform.tfvars`.
5. Set your public IP as `ssh_cidr` and choose a strong RDS password.
6. Run:

```bash
terraform init
terraform plan
terraform apply
```

Terraform provisions:
- Ubuntu EC2 instance
- EC2 security group
- MySQL RDS instance
- RDS security group allowing MySQL only from the EC2 security group
- RDS subnet group

### Application deployment on EC2

Terraform intentionally does not pretend to upload the whole source repository. After EC2 is ready, copy/clone this repository to `/opt/ibms`, build the services, and run Docker Compose.

For an AWS RDS-backed deployment, set these environment variables for each service:

```text
DB_HOST=<RDS_ENDPOINT>
DB_PORT=3306
DB_USERNAME=<RDS_USERNAME>
DB_PASSWORD=<RDS_PASSWORD>
```

Keep the four logical database names:

```text
auth_db
transaction_db
notification_db
loan_db
```

The services already read DB settings from environment variables, so no Java source-code change is required for the cloud database.

## Viva explanation

**Why is this both MSA and Cloud Computing?**

"The application is designed as four independent Spring Boot microservices—Auth, Transaction, Notification and Loan—with separate databases and OpenFeign communication. For cloud computing, I containerized the services using Docker, added an Nginx API gateway, externalized configuration through environment variables, and prepared AWS deployment using EC2 for compute and RDS for managed MySQL. Terraform provides infrastructure as code, while security groups handle network access. So the microservices layer defines the application architecture and the cloud layer provides scalable infrastructure and managed resources."
