terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ─── VPC y Red ────────────────────────────────────────────────────────────────

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.project_name}-vpc"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name    = "${var.project_name}-igw"
    Project = var.project_name
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name    = "${var.project_name}-subnet-public"
    Project = var.project_name
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name    = "${var.project_name}-rt-public"
    Project = var.project_name
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# ─── Security Groups ──────────────────────────────────────────────────────────

resource "aws_security_group" "app" {
  name        = "${var.project_name}-sg-app"
  description = "Security group para la aplicacion Spring Boot"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "API REST"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_allowed_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-sg-app"
    Project = var.project_name
  }
}

resource "aws_security_group" "mongodb" {
  name        = "${var.project_name}-sg-mongodb"
  description = "Security group para MongoDB"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MongoDB desde la app"
    from_port       = 27017
    to_port         = 27017
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-sg-mongodb"
    Project = var.project_name
  }
}

# ─── EC2: MongoDB ─────────────────────────────────────────────────────────────

resource "aws_instance" "mongodb" {
  ami                    = var.ami_id
  instance_type          = var.mongodb_instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.mongodb.id]
  key_name               = var.key_pair_name

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Instalar Docker
    apt-get update -y
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker

    # Levantar MongoDB con Docker
    docker run -d \
      --name mongodb \
      --restart unless-stopped \
      -e MONGO_INITDB_ROOT_USERNAME=${var.mongo_username} \
      -e MONGO_INITDB_ROOT_PASSWORD=${var.mongo_password} \
      -e MONGO_INITDB_DATABASE=${var.mongo_database} \
      -p 27017:27017 \
      -v /data/mongo:/data/db \
      mongo:7.0
  EOF

  tags = {
    Name        = "${var.project_name}-mongodb"
    Role        = "database"
    Environment = var.environment
    Project     = var.project_name
  }
}

# ─── EC2: Aplicación Spring Boot ──────────────────────────────────────────────

resource "aws_instance" "app" {
  ami                    = var.ami_id
  instance_type          = var.app_instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.app.id]
  key_name               = var.key_pair_name

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Instalar Docker
    apt-get update -y
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker

    # Esperar a que MongoDB esté listo
    sleep 30

    # Levantar la aplicación
    docker run -d \
      --name franquicias-api \
      --restart unless-stopped \
      -e MONGODB_URI=mongodb://${var.mongo_username}:${var.mongo_password}@${aws_instance.mongodb.private_ip}:27017/${var.mongo_database}?authSource=admin \
      -e MONGODB_DB=${var.mongo_database} \
      -p 8080:8080 \
      ${var.docker_image}
  EOF

  depends_on = [aws_instance.mongodb]

  tags = {
    Name        = "${var.project_name}-app"
    Role        = "application"
    Environment = var.environment
    Project     = var.project_name
  }
}

# ─── Elastic IP para la App ───────────────────────────────────────────────────

resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"

  tags = {
    Name    = "${var.project_name}-eip"
    Project = var.project_name
  }
}
