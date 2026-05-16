# 🏪 Franquicias API

API REST reactiva para gestión de franquicias, sucursales y productos.
Desarrollada con **Spring Boot 4.0.6 + WebFlux + MongoDB**.

---

## 🏗️ Arquitectura

```
franquicias-api/
├── src/main/java/com/accenture/franquicias_api/
│   ├── controller/       → Endpoints REST (WebFlux)
│   ├── service/          → Lógica de negocio reactiva
│   ├── repository/       → Acceso a MongoDB (ReactiveMongoRepository)
│   ├── model/            → Entidades del dominio
│   ├── dto/              → Objetos de request/response
│   └── exception/        → Manejo global de errores
├── Dockerfile            → Build multi-stage
├── docker-compose.yml    → App + MongoDB
└── .env.example          → Variables de entorno
```

**Modelo de datos:**
```
Franquicia
  ├── id, nombre
  └── sucursales[]
        ├── id, nombre
        └── productos[]
              ├── id, nombre, stock
```

---

## ⚙️ Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | 2.x |

---

## 🐳 Opción 1 — Levantar con Docker (recomendado)

```bash
# 1. Clonar el repositorio
git clone https://github.com/JDVB30/Accenture-2026.git
cd franquicias-api

# 2. Configurar variables de entorno
cp .env.example .env
# (opcional) editar .env con tus valores

# 3. Construir y levantar los servicios
docker-compose up --build

# La API estará disponible en:
# http://localhost:8080
```

Para detener los servicios:
```bash
docker-compose down
# Para eliminar también los volúmenes de datos:
docker-compose down -v
```

---

## 💻 Opción 2 — Levantar en local (sin Docker)

### 1. Levantar MongoDB local

```bash
# Con Docker solo para MongoDB:
docker run -d \
  --name mongo-local \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  mongo:7.0

# O si tienes MongoDB instalado localmente, simplemente iniciarlo:
mongod --dbpath /data/db
```

### 2. Configurar la conexión

Edita `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://admin:admin123@franquicias-mongodb:27017/franquicias_db?authSource=admin
```

O exporta la variable de entorno:

```bash
export MONGODB_URI=mongodb://admin:admin123@franquicias-mongodb:27017/franquicias_db?authSource=admin
```

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
# La API estará disponible en http://localhost:8080
```

### 4. Ejecutar los tests

```bash
./mvnw test
```

---

## 📡 Endpoints disponibles

### Franquicias

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/franquicias` | Listar todas las franquicias |
| `POST` | `/api/franquicias` | Crear nueva franquicia |
| `PUT` | `/api/franquicias/{id}/nombre` | Actualizar nombre de franquicia |

### Sucursales

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/franquicias/{fId}/sucursales` | Agregar sucursal |
| `PUT` | `/api/franquicias/{fId}/sucursales/{sId}/nombre` | Actualizar nombre de sucursal |

### Productos

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/franquicias/{fId}/sucursales/{sId}/productos` | Agregar producto |
| `DELETE` | `/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}` | Eliminar producto |
| `PUT` | `/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}/stock` | Actualizar stock |
| `PUT` | `/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}/nombre` | Actualizar nombre producto |
| `GET` | `/api/franquicias/{fId}/producto-mayor-stock` | Producto con más stock por sucursal |

---

## 🧪 Ejemplos de uso (curl)

### Crear una franquicia

```bash
curl -X POST http://localhost:8080/api/franquicias \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Franquicia Colombia"}'
```

**Respuesta:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Franquicia Colombia",
  "sucursales": []
}
```

### Agregar una sucursal

```bash
curl -X POST http://localhost:8080/api/franquicias/{franquiciaId}/sucursales \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Sucursal Bogotá"}'
```

### Agregar un producto

```bash
curl -X POST http://localhost:8080/api/franquicias/{fId}/sucursales/{sId}/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Hamburguesa Clásica", "stock": 150}'
```

### Actualizar stock

```bash
curl -X PUT http://localhost:8080/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 300}'
```

### Eliminar producto

```bash
curl -X DELETE http://localhost:8080/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}
```

### Producto con mayor stock por sucursal

```bash
curl http://localhost:8080/api/franquicias/{franquiciaId}/producto-mayor-stock
```

**Respuesta:**
```json
[
  {
    "sucursalId": "abc123",
    "sucursalNombre": "Sucursal Bogotá",
    "productoId": "xyz789",
    "productoNombre": "Hamburguesa Clásica",
    "stock": 300
  },
  {
    "sucursalId": "def456",
    "sucursalNombre": "Sucursal Medellín",
    "productoId": "uvw321",
    "productoNombre": "Papas Fritas",
    "stock": 500
  }
]
```

---

## ☁️ Infraestructura como código (Terraform)

La carpeta `terraform/` aprovisiona en AWS:
- VPC + Subnet pública + Internet Gateway
- Security Groups para app (8080) y MongoDB (27017)
- EC2 `t3.small` con la app Spring Boot en Docker
- EC2 `t3.micro` con MongoDB 7.0 en Docker
- Elastic IP fija para la app

### Despliegue en AWS

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars con tus valores

export AWS_ACCESS_KEY_ID=tu_access_key
export AWS_SECRET_ACCESS_KEY=tu_secret_key
export TF_VAR_mongo_password=tu_password_seguro

terraform init
terraform plan
terraform apply
```

Para destruir: `terraform destroy`

---

## 🛠️ Stack tecnológico

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.0.6 | Framework principal |
| Spring WebFlux | Programación reactiva (Mono/Flux) |
| Spring Data MongoDB Reactive | Persistencia reactiva |
| MongoDB 7.0 | Base de datos |
| Lombok | Reducción de boilerplate |
| Docker + Docker Compose | Empaquetado y orquestación |
| JUnit 5 + WebTestClient | Tests de integración |

---

## 📋 Criterios de aceptación cubiertos

- [x] Proyecto en Spring Boot
- [x] Endpoint para agregar franquicia
- [x] Endpoint para agregar sucursal
- [x] Endpoint para agregar producto
- [x] Endpoint para eliminar producto
- [x] Endpoint para modificar stock
- [x] Endpoint de producto con mayor stock por sucursal
- [x] Persistencia con MongoDB
- [x] Docker + Docker Compose *(plus)*
- [x] Programación reactiva con WebFlux *(plus)*
- [x] Actualizar nombre de franquicia *(plus)*
- [x] Actualizar nombre de sucursal *(plus)*
- [x] Actualizar nombre de producto *(plus)*
