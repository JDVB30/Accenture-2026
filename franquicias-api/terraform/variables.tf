variable "aws_region" {
  description = "Region de AWS donde se desplegará la infraestructura"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nombre del proyecto, usado como prefijo en todos los recursos"
  type        = string
  default     = "franquicias-api"
}

variable "environment" {
  description = "Ambiente de despliegue"
  type        = string
  default     = "production"

  validation {
    condition     = contains(["development", "staging", "production"], var.environment)
    error_message = "El ambiente debe ser: development, staging o production."
  }
}

variable "ami_id" {
  description = "AMI de Ubuntu 24.04 LTS (varía según región)"
  type        = string
  default     = "ami-0c7217cdde317cfec" # Ubuntu 24.04 LTS - us-east-1
}

variable "app_instance_type" {
  description = "Tipo de instancia EC2 para la aplicación Spring Boot"
  type        = string
  default     = "t3.small" # 2 vCPU, 2GB RAM - apto para Spring Boot + WebFlux
}

variable "mongodb_instance_type" {
  description = "Tipo de instancia EC2 para MongoDB"
  type        = string
  default     = "t3.micro" # 2 vCPU, 1GB RAM - suficiente para desarrollo/pruebas
}

variable "key_pair_name" {
  description = "Nombre del Key Pair de AWS para acceso SSH a las instancias"
  type        = string
}

variable "ssh_allowed_cidr" {
  description = "CIDR permitido para acceso SSH (reemplaza con tu IP)"
  type        = string
  default     = "0.0.0.0/0" # Cambiar a tu IP: "x.x.x.x/32"
}

variable "mongo_username" {
  description = "Usuario administrador de MongoDB"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "mongo_password" {
  description = "Contraseña de MongoDB (usar variable de entorno TF_VAR_mongo_password)"
  type        = string
  sensitive   = true
}

variable "mongo_database" {
  description = "Nombre de la base de datos MongoDB"
  type        = string
  default     = "franquicias_db"
}

variable "docker_image" {
  description = "Imagen Docker de la aplicación (ej: tu-usuario/franquicias-api:latest)"
  type        = string
  default     = "franquicias-api:latest"
}
