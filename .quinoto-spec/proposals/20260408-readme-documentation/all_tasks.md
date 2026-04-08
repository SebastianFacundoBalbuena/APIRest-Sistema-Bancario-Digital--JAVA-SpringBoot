# Plan de Tareas (Project Documentation README — All User Stories)

## Tareas por Historia de Usuario

### US-DOCU-001: README.md con estructura completa

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-001 | Config | Crear estructura básica README.md | Crear archivo README.md en raíz con título, descripción y sección de badges | US-DOCU-001 | documentación | `README.md` | XS | P1 | - |
| TSK-DOCU-002 | Config | Agregar tabla de contenidos | Insertar tabla de contenidos con enlaces a las secciones principales | US-DOCU-001 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 |
| TSK-DOCU-003 | Config | Agregar descripción del proyecto | Escribir descripción detallada del sistema bancario digital y sus funcionalidades | US-DOCU-001 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 |

### US-DOCU-002: Prerrequisitos e instalación

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-004 | Config | Crear sección Prerrequisitos | Agregar tabla con versiones de herramientas requeridas | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 |
| TSK-DOCU-005 | Config | Crear sección Instalación | Documentar pasos: clone, configure, docker-compose | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-004 |
| TSK-DOCU-006 | Config | Agregar alternativa local | Documentar ejecución sin Docker (Maven) | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-005 |

### US-DOCU-003: Estructura del proyecto

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-007 | Config | Agregar árbol de directorios | Insertar estructura completa del proyecto | US-DOCU-003 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 |
| TSK-DOCU-008 | Config | Describir capas architecture | Explicar domain, application, infrastructure | US-DOCU-003 | documentación | `README.md` | XS | P1 | TSK-DOCU-007 |

### US-DOCU-004: Comandos disponibles

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-009 | Config | Agregar comandos Maven | Documentar test, build, run | US-DOCU-004 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 |
| TSK-DOCU-010 | Config | Agregar comandos Docker | Documentar up, down, logs | US-DOCU-004 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 |

### US-DOCU-005: Variables de entorno

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-011 | Config | Crear tabla de variables | Documentar DB_URL, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION | US-DOCU-005 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 |

### US-DOCU-006: Endpoints API

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-012 | Config | Agregar tabla de endpoints | Listar endpoints principales con método y descripción | US-DOCU-006 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 |
| TSK-DOCU-013 | Config | Referenciar Swagger UI | Añadir enlace a documentación completa | US-DOCU-006 | documentación | `README.md` | XS | P2 | TSK-DOCU-012 |

### US-DOCU-007: Contribuir

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-014 | Config | Crear sección Contribuir | Instrucciones para branches, commits, PRs | US-DOCU-007 | documentación | `README.md` | XS | P3 | TSK-DOCU-001 |
| TSK-DOCU-015 | Config | Agregar convenciones | Describir Clean Architecture, validaciones, DI | US-DOCU-007 | documentación | `README.md` | XS | P3 | TSK-DOCU-014 |

### US-DOCU-008: Revisión y validación

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-016 | Test | Verificar comandos ejecutables | Probar cada comando documentado | US-DOCU-008 | documentación | - | XS | P1 | Todas las tareas |
| TSK-DOCU-017 | Test | Validar enlaces | Verificar que todos los enlaces funcionan | US-DOCU-008 | documentación | `README.md` | XS | P1 | Todas las tareas |
| TSK-DOCU-018 | Test | Verificar copy-paste | Confirmar que el código de ejemplo es ejecutable | US-DOCU-008 | documentación | `README.md` | XS | P1 | TSK-DOCU-016 |

## Resumen de Tareas

| Estado | Cantidad |
|--------|----------|
| Completadas | 18 |
| Pendientes | 0 |