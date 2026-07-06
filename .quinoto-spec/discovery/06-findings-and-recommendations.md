# 📝 Findings and Recommendations: Sistema Bancario Digital

**Guardar en:** `.quinoto-spec/discovery/06-findings-and-recommendations.md`

---

## 🔍 Informe de Descubrimientos

### Categorización de Hallazgos

| Categoría | Cantidad | Prioridad |
|-----------|----------|-----------|
| 🔴 Críticas | 3 | Alta |
| 🟡 Medias | 5 | Media |
| 🟢 Bajas | 4 | Baja |

---

## 🔴 Hallazgos Críticos (Alta Prioridad)

### 1. Credenciales Hardcodeadas en docker-compose.yml

**Severidad:** 🔴 Crítica  
**Ubicación:** `docker-compose.yml:15-21`

**Descripción:**
```yaml
environment:
  DB_PASSWORD: balbuena022000
  JWT_SECRET: 586E3272357538782F413F4428472B4B6250655368566B597033733676397924
```

Las credenciales de PostgreSQL y la clave JWT están expuestas en texto plano en el archivo de configuración.

**Riesgo:**
- Exposición de credenciales en repositorio (si se hace commit)
- Acceso no autorizado a base de datos
- Generación de tokens JWT comprometidos

**Recomendación:**
```bash
# Usar archivos .env
# .env
DB_PASSWORD=balbuena022000
JWT_SECRET=586E3...

# docker-compose.yml
env_file:
  - .env
```

**Owner:** DevOps / Security Team  
**Prioridad:** Inmediata

---

### 2. Falta de application.properties explícito

**Severidad:** 🔴 Crítica  
**Ubicación:** Proyecto completo

**Descripción:**
No existe archivo `src/main/resources/application.properties` o `application.yml`. La aplicación depende de valores por defecto de Spring Boot.

**Riesgo:**
- Comportamiento inesperado en diferentes entornos
- Sin configuración de producción explícita
- DDL auto en `create` puede borrar datos

**Recomendación:**
Crear `src/main/resources/application.properties`:
```properties
spring.application.name=banco
server.port=8080

# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

**Owner:** Backend Team  
**Prioridad:** Inmediata

---

### 3. Sin Gestión de Secretos

**Severidad:** 🔴 Crítica  
**Ubicación:** Infraestructura

**Descripción:**
No existe integración con herramientas de gestión de secretos (Vault, AWS Secrets Manager, etc.)

**Riesgo:**
- Secrets en variables de entorno del sistema
- Sin rotación automática de credenciales
- Dificultad para auditar acceso a secretos

**Recomendación:**
1. Implementar **HashiCorp Vault** o **AWS Secrets Manager**
2. Rotar credenciales cada 90 días
3. Auditoría de acceso a secretos

**Owner:** DevOps / Security  
**Prioridad:** Alta

---

## 🟡 Hallazgos de Prioridad Media

### 4. Logs usando System.out en lugar de Logger

**Severidad:** 🟡 Media  
**Ubicación:** 
- `Cliente.java:33, 83, 96`
- `AperturaCuentaService.java:96, 141`
- Múltiples servicios

**Descripción:**
```java
System.out.println("✅ Cliente creado: " + nombre + " (" + email + ")");
System.err.println(" Error en apertura de cuenta: " + e.getMessage());
```

**Riesgo:**
- No configurable (no se puede apagar en producción)
- Sin niveles de log (INFO, DEBUG, ERROR)
- Performance overhead

**Recomendación:**
```java
private static final Logger log = LoggerFactory.getLogger(Cliente.class);

// Uso
log.info("Cliente creado: {} ({})", nombre, email);
log.error("Error en apertura de cuenta: {}", e.getMessage());
```

**Owner:** Backend Team  
**Prioridad:** Media

---

### 5. Sin Tests de Cobertura

**Severidad:** 🟡 Media  
**Ubicación:** `pom.xml`

**Descripción:**
No hay plugin de cobertura configurado (JaCoCo, etc.)

**Riesgo:**
- No se sabe qué porcentaje del código está testeado
- Áreas críticas pueden no tener tests

**Recomendación:**
Agregar a `pom.xml`:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Owner:** QA Team  
**Prioridad:** Media

---

### 6. Sin Pipeline CI/CD

**Severidad:** 🟡 Media  
**Ubicación:** Raíz del proyecto

**Descripción:**
No hay workflow de GitHub Actions, GitLab CI, Jenkins, etc.

**Riesgo:**
- Sin build automático
- No se ejecutan tests en PRs
- Deploy manual propenso a errores

**Recomendación:**
Crear `.github/workflows/ci.yml` (ver archivo 05-devops-ci-security.md)

**Owner:** DevOps  
**Prioridad:** Media

---

### 7. Sin Rate Limiting en Endpoints

**Severidad:** 🟡 Media  
**Ubicación:** Controllers

**Descripción:**
No hay protección contra ataques de fuerza bruta o abuso de API.

**Riesgo:**
- DDoS
- Fuerza bruta en `/auth/login`
- Abuso de recursos

**Recomendación:**
```java
// Agregar a application.properties
spring.security.rate-limit.enabled=true
spring.security.rate-limit.requests-per-second=10
```

O implementar manualmente con Bucket4j.

**Owner:** Backend / Security  
**Prioridad:** Media

---

### 8. Transacciones no compensadas en errores

**Severidad:** 🟡 Media  
**Ubicación:** `TransaccionService.java`

**Descripción:**
En transferencias, si falla el crédito a cuenta destino, no se revierte el débito de origen.

**Riesgo:**
- Inconsistencia de datos
- Fondos perdidos

**Recomendación:**
Implementar Saga Pattern o transacciones distribuidas.

**Owner:** Backend Team  
**Prioridad:** Media

---

## 🟢 Hallazgos de Prioridad Baja

### 9. Validación de email básica

**Severidad:** 🟢 Baja  
**Ubicación:** `Cliente.java:123-129`

```java
private String validarEmail(String email){
    if(email == null || email.trim().isEmpty()) throw new IllegalArgumentException(...)
    if(!email.contains("@")) throw new IllegalArgumentException(...)
    return email.trim();
}
```

**Recomendación:** Usar validación RFC 5322 con Apache Commons Validator.

---

### 10. Sin documentação en código (JavaDoc)

**Severidad:** 🟢 Baja  
**Ubicación:** Todos los archivos

**Recomendación:** Agregar JavaDoc a métodos públicos.

---

### 11. CORS no configurado explícitamente

**Severidad:** 🟢 Baja  
**Ubicación:** `SecurityConfig.java`

**Recomendación:** Configurar CORS explícitamente para frontend.

---

### 12. Sin health check endpoint

**Severidad:** 🟢 Baja  
**Ubicación:** N/A

**Recomendación:** Agregar Spring Actuator:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## 📋 Checklist de Seguimiento

| # | Hallazgo | Prioridad | Owner | Estado |
|---|----------|-----------|-------|--------|
| 1 | Credenciales hardcodeadas | 🔴 Alta | DevOps | ⏳ Pendiente |
| 2 | Falta application.properties | 🔴 Alta | Backend | ⏳ Pendiente |
| 3 | Sin gestión de secretos | 🔴 Alta | DevOps/Security | ⏳ Pendiente |
| 4 | System.out en logs | 🟡 Media | Backend | ⏳ Pendiente |
| 5 | Sin cobertura de tests | 🟡 Media | QA | ⏳ Pendiente |
| 6 | Sin CI/CD | 🟡 Media | DevOps | ⏳ Pendiente |
| 7 | Sin rate limiting | 🟡 Media | Backend/Security | ⏳ Pendiente |
| 8 | Transacciones no compensadas | 🟡 Media | Backend | ⏳ Pendiente |
| 9 | Validación email básica | 🟢 Baja | Backend | ⏳ Pendiente |
| 10 | Sin JavaDoc | 🟢 Baja | Backend | ⏳ Pendiente |
| 11 | CORS no configurado | 🟢 Baja | Backend | ⏳ Pendiente |
| 12 | Sin health check | 🟢 Baja | DevOps | ⏳ Pendiente |

---

## 🗺️ Roadmap de Mejoras

### Corto Plazo (1-2 semanas)

- [ ] Crear `application.properties` con variables de entorno
- [ ] Mover credenciales a archivo `.env`
- [ ] Implementar Logging con SLF4J

### Medio Plazo (1-2 meses)

- [ ] Configurar pipeline CI/CD
- [ ] Agregar JaCoCo para cobertura
- [ ] Implementar rate limiting
- [ ] Agregar Spring Actuator

### Largo Plazo (3-6 meses)

- [ ] Implementar gestión de secretos (Vault)
- [ ] Implementar Saga Pattern para transacciones
- [ ] Auditoría de seguridad completa
- [ ] Docs y JavaDoc completos