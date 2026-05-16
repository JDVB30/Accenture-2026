output "app_public_ip" {
  description = "IP pública de la aplicación Spring Boot"
  value       = aws_eip.app.public_ip
}

output "app_url" {
  description = "URL base de la API REST"
  value       = "http://${aws_eip.app.public_ip}:8080/api"
}

output "mongodb_private_ip" {
  description = "IP privada de la instancia MongoDB (uso interno)"
  value       = aws_instance.mongodb.private_ip
}

output "ssh_app_command" {
  description = "Comando SSH para conectarse a la instancia de la app"
  value       = "ssh -i ${var.key_pair_name}.pem ubuntu@${aws_eip.app.public_ip}"
}

output "ssh_mongodb_command" {
  description = "Comando SSH para conectarse a la instancia de MongoDB"
  value       = "ssh -i ${var.key_pair_name}.pem ubuntu@${aws_instance.mongodb.public_ip}"
}

output "vpc_id" {
  description = "ID de la VPC creada"
  value       = aws_vpc.main.id
}
