# 🛠️ Stack Profile: Sistema Bancario Digital

**Guardar en:** `.quinoto-spec/discovery/00-stack-profile.md`

**Discovery Date:** 2026-04-08

---

## 🏗️ Core Technologies

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 3.5.7 | Framework principal |
| **Spring Data JPA** | (incluido en Spring Boot) | Acceso a datos |
| **PostgreSQL** | 17 | Base de datos relacional |
| **Maven** | (wrapper) | Gestor de dependencias y build |

---

## 📦 Dependencias Principales (pom.xml)

- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - ORM/Hibernate
- `spring-boot-starter-validation` - Validación de inputs
- `spring-boot-starter-security` - Seguridad y JWT
- `springdoc-openapi-starter-webmvc-ui` - 2.8.6 - Documentación Swagger/OpenAPI
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - 0.11.5 - Autenticación JWT
- `postgresql` - Driver de base de datos
- `lombok` - Reducción de boilerplate
- `h2` - Base de datos en memoria (testing)

---

## 🧪 Quality & Testing

| Herramienta | Propósito |
|------------|-----------|
| **JUnit 5 (Jupiter)** | Test runner principal |
| **AssertJ** | Assertions fluent |
| **Mockito** | Mocking de dependencias |
| **Spring Boot Test** | Testing de integración |

**Tipos de tests detectados:**
- **Unitarios**: Entidades domain (`ClienteTest`, `CuentaTest`, `TransaccionTest`), Value Objects (`DineroTest`, `ClienteIdTest`, etc.)
- **Integración**: Repositorios JPA (`ClienteRepositoryJpaTest`, `CuentaRepositoryJpaTest`, `TransaccionRepositoryJpaTest`)
- **Controladores**: (`ClienteControllerTest`, `CuentaControllerTest`, `TransaccionControllerTest`)
- **Servicios**: (`AperturaCuentaServiceTest`, `TransaccionServiceTest`, `ConsultaSaldoServiceTest`, `GestionClienteServiceTest`)

**Comando para ejecutar tests:**
```bash
cd Proyecto-Sistema-de-banco-digital && ./mvnw test
```

**Cobertura:** No se detectó configuración de plugins de cobertura (Jacoco, etc.). Se recomienda agregar para medir cobertura.

---

## 🚢 Infrastructure & DevOps

| Tecnología | Propósito |
|------------|-----------|
| **Docker** | Contenedores |
| **docker-compose** | Orquestación de servicios |
| **PostgreSQL 17 Alpine** | Base de datos en contenedor |

---

## 📝 Coding Standards (Detectados)

### Arquitectura Detectada: **Clean Architecture / Hexagonal**
- **Domain Layer**: Entidades (`Cliente`, `Cuenta`, `Transaccion`) y Value Objects (`Dinero`, `ClienteId`, `CuentaId`, etc.)
- **Application Layer**: Servicios (`AperturaCuentaService`, `TransaccionService`, etc.) y DTOs
- **Infrastructure Layer**: Controladores REST, Repositorios JPA, Mappers, Seguridad JWT
- **Ports/Out**: Interfaces de repositorio (`ClienteRepository`, `CuentaRepository`, `TransaccionRepository`)

### Patrones de Diseño
- **Dependency Injection** (constructor injection en todos los servicios)
- **Value Objects** (inmutables para dinero, IDs, tipos)
- **Repository Pattern** (interfaces en application layer, implementaciones en infrastructure)
- **Service Layer** (lógica de negocio encapsulada)

### Convenciones de Código
- Nombres en español para métodos y variables
- Inyección de dependencias por constructor (no `@Autowired` en campos)
- Anotaciones `@Transactional` en servicios
- Validación de requests con Jakarta Bean Validation (`@Valid`)
- Manejo centralizado de excepciones con `GlobalExceptionHandler`

---

## 📋 Resumen de Estructura

```
Proyecto-Sistema-de-banco-digital/
├── src/main/java/com/banco/
│   ├── domain/model/
│   │   ├── entities/        # Cliente, Cuenta, Transaccion
│   │   └── valueobjects/    # Dinero, ClienteId, Moneda, etc.
│   ├── application/
│   │   ├── services/        # AperturaCuentaService, TransaccionService
│   │   ├── dto/             # Request/Response DTOs
│   │   └── port/out/        # Interfaces de repositorio
│   └── infrastructure/
│       ├── controllers/     # REST endpoints
│       ├── persistence/     # JPA repositories, mappers, entities
│       ├── security/jwt/    # JWT utils, filters, config
│       └── config/          # Configuración
├── src/test/
│   └── java/com/banco/      # Tests unitarios e integración
├── pom.xml
└── Dockerfile
```