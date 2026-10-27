# IBMS AWS deployment design

This is a deployment guide, not evidence that AWS resources have been created. The verified application deployment is local Docker Compose. Do not provision AWS until the account owner has reviewed the plan, charges, and student-account limits.

## Architecture

- EC2 runs Docker Compose, the four Spring Boot services, and the Nginx API gateway on port 80.
- RDS for MySQL is private and hosts `auth_db`, `transaction_db`, `notification_db`, and `loan_db`.
- The EC2 security group allows HTTP on port 80 and SSH only from the configured `ssh_cidr`.
- The RDS security group allows TCP 3306 only from the EC2 security group.
- Terraform declares EC2, RDS, the RDS DB subnet group, and both security groups.

## Provision

From `cloud/terraform`, create a local `terraform.tfvars` from the example. Set your own public IP as `ssh_cidr`; provide credentials using local ignored configuration or `TF_VAR_db_password`.

```sh
terraform init -backend=false
terraform validate
terraform plan
# Review the full plan and cost before explicitly choosing to apply.
terraform apply
```

Only `terraform init -backend=false` and `terraform validate` have been run for this project. No plan or apply was run, and no AWS resources were created. EC2, RDS, storage, and data transfer can incur charges.

## Configure and deploy

After an approved apply, copy/clone the repository to `/opt/ibms` on the EC2 instance. Get the database endpoint with `terraform output -raw rds_endpoint`. On EC2:

```sh
cd /opt/ibms
cp .env.aws.example .env.aws
# Set MYSQL_RDS_HOST, DB_USERNAME, DB_PASSWORD in the untracked .env.aws
./scripts/deploy-aws-ec2.sh
```

The AWS Compose file requires the RDS endpoint and credentials from environment variables, uses the internal service URLs for OpenFeign calls by default, and runs a one-shot `db-init` container before the application services. That container executes `cloud/mysql/init/01-create-databases.sql` against RDS to create all four logical databases. Do not publish `.env.aws` or put credentials in Git.

## Verify

From a client allowed by the EC2 security group:

```sh
curl -i http://<EC2_PUBLIC_IP>/health
curl -i -X POST http://<EC2_PUBLIC_IP>/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Cloud Demo","email":"cloud-demo@example.test","password":"DemoPass123!","phone":"555-0100"}'
# Use the returned user id for the other API requests.
curl -i -X POST http://<EC2_PUBLIC_IP>/accounts \
  -H 'Content-Type: application/json' -d '{"userId":<USER_ID>}'
curl -i -X POST http://<EC2_PUBLIC_IP>/notifications \
  -H 'Content-Type: application/json' \
  -d '{"userId":<USER_ID>,"message":"Cloud demo","type":"DEMO"}'
curl -i -X POST http://<EC2_PUBLIC_IP>/loans/apply \
  -H 'Content-Type: application/json' -d '{"userId":<USER_ID>,"amount":1000}'
docker compose --env-file .env.aws -f docker-compose.aws.yml ps
docker compose --env-file .env.aws -f docker-compose.aws.yml logs --tail=100
```

Replace placeholders before running the commands. The gateway is exposed on EC2 port 80; the RDS database is not public.

## Cleanup

When the cloud environment is no longer needed, review the data-loss implications and run `terraform destroy` from `cloud/terraform`. This deletes provisioned infrastructure and RDS data. Keep Terraform state private because it can contain sensitive values.

## Kubernetes

Kubernetes is experimental and is not part of this submission. The local Docker Desktop cluster remains paused. No Kubernetes resources, image uploads, or AWS resources are required for the local Compose demonstration.
