variable "aws_region" {
  default = "ap-south-1"
}

variable "project_name" {
  default = "ibms-cloud"
}

variable "instance_type" {
  default = "t3.micro"
}

variable "db_instance_class" {
  default = "db.t3.micro"
}

variable "db_username" {
  default = "ibmsadmin"
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}

variable "ssh_cidr" {
  description = "Your public IP in CIDR notation, e.g. 203.0.113.10/32"
  type        = string
}
