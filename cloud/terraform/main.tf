terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]
  filter { name = "name" values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"] }
  filter { name = "virtualization-type" values = ["hvm"] }
  filter { name = "root-device-type" values = ["ebs"] }
}

resource "aws_security_group" "ibms_ec2" {
  name        = "${var.project_name}-ec2-sg"
  description = "IBMS EC2 access"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr]
  }

  ingress {
    description = "HTTP API gateway"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ibms_rds" {
  name        = "${var.project_name}-rds-sg"
  description = "MySQL access only from IBMS EC2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "MySQL from EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ibms_ec2.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_subnet_group" "ibms" {
  name       = var.project_name
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "ibms" {
  identifier             = var.project_name
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = var.db_instance_class
  allocated_storage      = 20
  max_allocated_storage  = 50
  db_name                = "auth_db"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.ibms.name
  vpc_security_group_ids = [aws_security_group.ibms_rds.id]
  publicly_accessible    = false
  skip_final_snapshot    = true
  deletion_protection    = false
}

resource "aws_instance" "ibms" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  vpc_security_group_ids = [aws_security_group.ibms_ec2.id]

  user_data = <<-USERDATA
    #!/bin/bash
    set -eux
    apt-get update -y
    apt-get install -y docker.io docker-compose-plugin git
    systemctl enable --now docker
    usermod -aG docker ubuntu
    mkdir -p /opt/ibms
    echo "IBMS EC2 host ready. Copy/clone the project into /opt/ibms and run Docker Compose."
    echo "RDS endpoint: ${aws_db_instance.ibms.address}" > /opt/ibms/cloud-endpoint.txt
  USERDATA

  tags = {
    Name = var.project_name
  }
}
