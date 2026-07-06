# Sistema Bancario Digital

[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/technologies/downloads/#java17)
[![Spring Boot 3.5.7](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-blue)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat&logo=apache-maven)](https://maven.apache.org/)

API REST para gestión de operaciones bancarias digitales. Permite gestionar clientes, cuentas bancarias, transacciones (depósitos, retiros, transferencias) y autenticación JWT.

## Tabla de Contenidos

- [Descripción](#descripción)
- [Prerrequisitos](#prerrequisitos)
- [Instalación](#instalación)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Comandos Disponibles](#comandos-disponibles)
- [Variables de Entorno](#variables-de-entorno)
- [Endpoints API](#endpoints-api)
- [Testing](#testing)
- [Docker](#docker)
- [Contribuir](#contribuir)

## Descripción

Sistema de gestión bancaria desarrollado con **Java 17** y **Spring Boot 3.5.7**, siguiendo principios de **Clean Architecture**. Proporciona una API REST para operaciones financieras básicas:

- ✅ Gestión de clientes (CRUD)
- ✅ Apertura y cierre de cuentas bancarias
- ✅ Transacciones: depósitos, retiros, transferencias
- ✅ Autenticación JWT segura
- ✅ Documentación automática con Swagger/OpenAPI

## Prerrequisitos

| Herramienta | Versión Mínima |
|-------------|----------------|
| Java JDK | 17 |
| Maven | 3.9+ |
| Docker | 24.0+ |
| Docker Compose | 2.0+ |

## Instalación

### 1. Clonar el repositorio

```bash
git clone <repo-url>
cd APIRest-Sistema-Bancario-Digital--JAVA-SpringBoot
```

### 2. Configurar variables de entorno (opcional)

Crear archivo `.env` en la raíz:

```env
DB_PASSWORD=balbuena022000
JWT_SECRET=586E3272357538782F413F4428472B4B6250655368566B597033733676397924
```

### 3. Levantar servicios con Docker

```bash
docker-compose up --build
```

Esto iniciarán:
- **App**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5433`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### 4. Alternativa: Ejecución local sin Docker

```bash
cd Proyecto-Sistema-de-banco-digital

# Compilar
./mvnw clean install

# Ejecutar (requiere PostgreSQL local configurado)
./mvnw spring-boot:run
```

## Estructura del Proyecto

```
APIRest-Sistema-Bancario-Digital--JAVA-SpringBoot/
├── Proyecto-Sistema-de-banco-digital/
│   ├── src/main/java/com/banco/
│   │   ├── domain/model/           # Entidades y Value Objects
│   │   │   ├── entities/          # Cliente, Cuenta, Transaccion
│   │   │   └── valueobjects/      # Dinero, ClienteId, Moneda, etc.
│   │   ├── application/          # Servicios y DTOs
│   │   │   ├── services/          # AperturaCuentaService, TransaccionService
│   │   │   ├── dto/               # Request/Response objects
│   │   │   └── port/out/          # Interfaces de repositorio
│   │   └── infrastructure/       # Implementaciones
│   │       ├── controllers/       # REST endpoints
│   │       ├── persistence/      # JPA repositories
│   │       └── security/jwt/      # Autenticación
│   └── src/test/                  # Tests
├── docker-compose.yml             # Orquestación Docker
├── Dockerfile                      # Imagen de la app
└── README.md                       # Este archivo
```

## Comandos Disponibles

| Comando | Descripción |
|---------|-------------|
| `./mvnw test` | Ejecutar todos los tests |
| `./mvnw clean install` | Compilar proyecto |
| `./mvnw spring-boot:run` | Ejecutar aplicación |
| `docker-compose up` | Iniciar servicios Docker |
| `docker-compose down` | Detener servicios |

## Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_URL` | JDBC URL | `jdbc:postgresql://postgres:5432/banco_db` |
| `DB_USERNAME` | Usuario BD | `postgres` |
| `DB_PASSWORD` | Password BD | `balbuena022000` |
| `JWT_SECRET` | Clave JWT | (configurar) |
| `JWT_EXPIRATION` | Expiración token (ms) | `86400000` |

## Endpoints API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/register` | Registrar usuario |
| POST | `/auth/login` | Iniciar sesión |
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes/{id}` | Obtener cliente |
| POST | `/api/cuentas` | Abrir cuenta |
| GET | `/api/cuentas` | Consultar saldo |
| POST | `/api/transacciones/transferir` | Transferencia |
| POST | `/api/transacciones/deposito` | Depósito |
| POST | `/api/transacciones/retiro` | Retiro |

**Documentación completa**: Acceder a `http://localhost:8080/swagger-ui/index.html`

## Testing

```bash
# Ejecutar todos los tests
cd Proyecto-Sistema-de-banco-digital
./mvnw test

# Tests con verbose output
./mvnw test -Dsurefire.useFile=false
```

## Docker

### Build manual

```bash
docker build -t banco-app:latest ./Proyecto-Sistema-de-banco-digital
```

### Compose completo

```bash
# Iniciar
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener
docker-compose down
```

## Contribuir

1. Crear branch desde `main`: `git checkout -b feature/NOMBRE`
2. Realizar cambios y commit
3. Push: `git push origin feature/NOMBRE`
4. Crear Pull Request

### Convenciones

- Seguir Clean Architecture
- Tests unitarios para lógica de negocio
- Validaciones con Jakarta Bean Validation
- Inyección de dependencias por constructor

---

**Documentación generada por:** Gustavo "Barba" Ghioldi con [QuinotoSpec](https://github.com/Quinoto-Tech/QuinotoSpec/), opencode + Big Pickle

**Licencia**: MIT
**Versión**: 1.0.0
**Fecha**: 2026-04-08