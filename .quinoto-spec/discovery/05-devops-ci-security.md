# 🚀 DevOps, CI/CD and Security: Sistema Bancario Digital

**Guardar en:** `.quinoto-spec/discovery/05-devops-ci-security.md`

---

## 🛠️ Configuración de Entorno

### Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo | Sensible |
|----------|-------------|---------|----------|
| `DB_URL` | URL de conexión JDBC | `jdbc:postgresql://postgres:5432/banco_db` | No |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` | ⚠️ Sí |
| `DB_PASSWORD` | Password de PostgreSQL | `balbuena022000` | 🔴 Sí |
| `JWT_SECRET` | Clave secreta para JWT | `586E3272357538782F413F4428472B4B...` | 🔴 Sí |
| `JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) | No |

### application.properties/application.yml

> ⚠️ **CRÍTICO**: No se encontró archivo de configuración `application.properties` o `application.yml` en el proyecto. La aplicación usa valores por defecto.

**Se recomienda crear** `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/banco_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=${JWT_SECRET:}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Server
server.port=8080
```

---

## 🐳 Docker y docker-compose

### docker-compose.yml

```yaml
version: '3.8'

services:
  app:
    build: ./Proyecto-Sistema-de-banco-digital
    container_name: banco_app
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/banco_db
      DB_USERNAME: postgres
      DB_PASSWORD: balbuena022000
      JWT_SECRET: 586E3272357538782F413F4428472B4B6250655368566B597033733676397924
      JWT_EXPIRATION: "86400000"
    depends_on:
      postgres:
        condition: service_healthy

  postgres:
    image: postgres:17-alpine
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: banco_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: balbuena022000
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Dockerfile

```dockerfile
#build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🔄 Pipelines CI/CD

**Estado:** ❌ No se detectó pipeline CI/CD en el proyecto.

### Recomendaciones

1. **GitHub Actions** (`.github/workflows/`):
   - Build y test en cada push
   - Análisis estático (SonarQube)
   - Build de imagen Docker
   - Despliegue a staging/producción

2. **Flujo sugerido**:
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package -DskipTests
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t banco-app:${{ github.sha }} .
```

---

## ⚙️ Scripts de Automatización

### Scripts detectados

| Script | Propósito |
|--------|-----------|
| `mvnw` / `mvnw.cmd` | Maven Wrapper |
| `docker-compose up` | Levantar servicios |

### Scripts faltantes (recomendados)

- `scripts/init-db.sh` - Inicialización de BD
- `scripts/backup.sh` - Backup de datos
- `scripts/deploy.sh` - Despliegue automatizado

---

## 🔒 Revisión de Seguridad

### ✅ Prácticas Implementadas

1. **Autenticación JWT** con `jjwt` 0.11.5
2. **Password encoding** (BCrypt detectado en código de AuthService)
3. **Validaciones de entrada** con `@Valid` y Bean Validation
4. **Transacciones** con `@Transactional`
5. **Preparación para HTTPS** en config de Spring

### ⚠️ Vulnerabilidades y Mejoras

#### 🔴 Críticas

1. **Credenciales hardcodeadas en docker-compose.yml**
   - Password de PostgreSQL visible
   - JWT Secret visible
   - **Acción**: Usar Docker secrets o variables de entorno externas

2. **Sin gestión de secretos**
   - No se usa Vault, AWS Secrets Manager, etc.
   - **Acción**: Implementar gestión de secretos

3. **JWT Secret debil**
   - Longitud de 64 caracteres (aparentemente)
   - **Acción**: Usar secret de al menos 256 bits generado con CSPRNG

#### 🟡 Medias

4. **Sin HTTPS en desarrollo**
   - La config no fuerza SSL
   - **Acción**: Configurar `server.ssl.enabled` para producción

5. **Logs con System.out**
   - `System.out.println` en código de dominio
   - **Acción**: Usar SLF4J/Logback

6. **Sin rate limiting**
   - Endpoints sin protección contra abuso
   - **Acción**: Implementar Spring Security rate limiting

#### 🟢 Bajas

7. **No hay auditorías de seguridad programadas**
8. **Sin headers de seguridad** (CSP, X-Frame-Options, etc.)
9. **CORS no configurado explícitamente**

---

## 📊 Auditoría de Dependencias

### Herramienta: Maven Dependency Plugin

```bash
cd Proyecto-Sistema-de-banco-digital
./mvnw dependency:analyze
```

### Vulnerabilidades Conocidas de Dependencias

**jjwt 0.11.5:**
- Verificar CVE-2022-25883 (vulnerabilidad en versiones anteriores)
- **Recomendación**: Verificar que la versión sea segura

**Spring Boot 3.5.7:**
- Mantener actualizado con parches de seguridad

**PostgreSQL Driver:**
- Actualizar regularmente

### Recomendaciones

1. **Agregar OWASP Dependency Check**:
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
</plugin>
```

2. **Ejecutar**:
```bash
./mvnw org.owasp:dependency-check-maven:check
```

---

## 💾 Estrategias de Backup

### Backups Detectados

| Tipo | Estado |
|------|--------|
| PostgreSQL volume Docker | ✅ Volumen persists |
| Backup automatizado | ❌ No detectado |
| Versionado de datos | ❌ No detectado |

### Recomendaciones

1. **Backup de PostgreSQL**:
```bash
# Backup manual
docker exec banco_postgres pg_dump -U postgres banco_db > backup.sql

# Backup automatizado (cron)
0 2 * * * docker exec banco_postgres pg_dump -U postgres banco_db > /backups/banco_db_$(date +\%Y\%m\%d).sql
```

2. **Estrategia 3-2-1**:
   - 3 copias de datos
   - 2 medios diferentes
   - 1 copia off-site

---

## 🧪 Comandos de Utility

```bash
# Compilar proyecto
cd Proyecto-Sistema-de-banco-digital
./mvnw clean install

# Ejecutar tests
./mvnw test

# Ejecutar con cobertura
./mvnw test -Djacoco.skip=false

# Ver dependencias
./mvnw dependency:tree

# Analizar vulnerabilidades
./mvnw dependency:analyze

# Build Docker
docker build -t banco-app:latest ./Proyecto-Sistema-de-banco-digital

# Levantar entorno completo
docker-compose up --build

# Ver logs
docker-compose logs -f app
```

---

## ✅ Checklist de DevOps

- [x] Dockerfile existente
- [x] docker-compose.yml configurado
- [ ] application.properties con variables de entorno
- [ ] Pipeline CI/CD implementado
- [ ] Gestión de secretos implementada
- [ ] Backup automatizado configurado
- [ ] Logs centralizados
- [ ] Health checks implementados