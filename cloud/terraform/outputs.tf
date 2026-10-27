output "ec2_public_ip" {
  value = aws_instance.ibms.public_ip
}

output "rds_endpoint" {
  value = aws_db_instance.ibms.address
}

output "rds_port" {
  value = aws_db_instance.ibms.port
}
