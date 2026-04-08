# 📋 Overview: Sistema Bancario Digital

**Guardar en:** `.quinoto-spec/discovery/01-overview.md`

---

## 📌 Resumen Ejecutivo

Sistema REST API para gestión de operaciones bancarias digitales desarrollado en **Java 17 con Spring Boot 3.5.7**. Permite gestionar clientes, cuentas bancarias, transacciones (depósitos, retiros, transferencias) y operaciones de autenticación JWT.

El proyecto implementa una arquitectura **Clean Architecture** con separación clara entre capas de dominio, aplicación e infraestructura.

---

## 🗂️ Estructura de Carpetas Principales

```
.
├── Proyecto-Sistema-de-banco-digital/
│   ├── src/main/java/com/banco/
│   │   ├── BancoApplication.java           # Punto de entrada
│   │   ├── domain/model/                   # Entidades y Value Objects
│   │   ├── application/                    # Servicios y DTOs
│   │   └── infrastructure/
│   │       ├── controllers/                # REST API
│   │       ├── persistence/                # JPA
│   │       ├── security/jwt/               # Autenticación
│   │       └── config/
│   ├── src/test/                           # Tests automatizados
│   ├── pom.xml                             # Dependencias Maven
│   └── Dockerfile                          # Imagen contenedor
├── docker-compose.yml                      # PostgreSQL + App
└── .gitignore
```

---

## 🚀 Comandos para Ejecutar la Aplicación

### Prerrequisitos
- **Java 17** instalado
- **Maven** (wrapper incluido en proyecto)
- **Docker** y **Docker Compose** (opcional, para entorno completo)

### Desarrollo Local (sin Docker)

```bash
# Compilar
cd Proyecto-Sistema-de-banco-digital
./mvnw clean install

# Ejecutar (requiere PostgreSQL local o configurar application.properties)
./mvnw spring-boot:run
```

> ⚠️ **Nota:** No se encontró archivo `application.properties`/`application.yml` en el proyecto. La aplicación usa configuración por defecto o variables de entorno. Verificar configuración antes de ejecutar.

### Entorno Docker (Completo)

```bash
# Iniciar servicios (PostgreSQL + App)
docker-compose up --build

# La API estará disponible en: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### Tests

```bash
cd Proyecto-Sistema-de-banco-digital
./mvnw test              # Ejecutar todos los tests
./mvnw verify            # Tests + verificación
```

---

## 📡 Puertos y Endpoints Principales

| Servicio | Puerto | Endpoint base |
|----------|--------|---------------|
| API REST | 8080 | `/api/*` |
| Swagger UI | 8080 | `/swagger-ui.html` |
| PostgreSQL | 5433 | (externo) / 5432 (interno) |

---

## ⚠️ Puntos Críticos Identificados

1. **Sin configuración de aplicación** - No existe `application.properties`/`application.yml` explícito; la app depende de valores por defecto o variables de entorno.

2. **Credenciales hardcodeadas** - En `docker-compose.yml` las credenciales de PostgreSQL están en texto plano:
   - Usuario: `postgres`
   - Password: `balbuena022000`
   - JWT Secret expuesta en variables de entorno

3. **Sin gestión de secretos** - No se detecta uso de herramientas como Vault, Secrets Manager, o archivos `.env`.

4. **Validaciones en cliente** - Verificaciones como `System.out.println` para logs de éxito/error (líneas 33, 83, 96, etc. en `Cliente.java`); no apropiada para producción.

5. **Limitación de máximo 5 cuentas por cliente** - Codificada en `Cliente.java:31` como constante, no configurable.

---

## 📦 Dependencias Clave

- Spring Boot 3.5.7
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt 0.11.5)
- SpringDoc OpenAPI (Swagger) 2.8.6
- PostgreSQL Driver
- Lombok

---

## ✅ Checklist de Salud del Proyecto

- [x] Proyecto Maven con `pom.xml` válido
- [x] Tests detectados (JUnit 5, Mockito)
- [x] docker-compose.yml para entorno completo
- [x] Dockerfile para la aplicación
- [ ] application.properties/yml para configuración
- [ ] Pipeline CI/CD detectado
- [ ] Gestión de secretos implementada