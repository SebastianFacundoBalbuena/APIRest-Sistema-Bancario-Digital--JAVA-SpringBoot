# Plan de Tareas (Project Documentation README — US-DOCU-001)

| ID | Tipo | Título | Descripción | Historia Relacionada | Servicio | Archivos a Modificar | Estimación | Prioridad | Dependencias | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TSK-DOCU-001 | Config | Crear estructura básica README.md | Crear archivo README.md en raíz con título, descripción y sección de badges | US-DOCU-001 | documentación | `README.md` | XS | P1 | - | [x] |
| TSK-DOCU-002 | Config | Agregar tabla de contenidos | Insertar tabla de contenidos con enlaces a las secciones principales | US-DOCU-001 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 | [x] |
| TSK-DOCU-003 | Config | Agregar descripción del proyecto | Escribir descripción detallada del sistema bancario digital y sus funcionalidades | US-DOCU-001 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 | [x] |
| TSK-DOCU-004 | Config | Crear sección Prerrequisitos | Agregar tabla con versiones de herramientas requeridas | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 | [x] |
| TSK-DOCU-005 | Config | Crear sección Instalación | Documentar pasos: clone, configure, docker-compose | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-004 | [x] |
| TSK-DOCU-006 | Config | Agregar alternativa local | Documentar ejecución sin Docker (Maven) | US-DOCU-002 | documentación | `README.md` | XS | P1 | TSK-DOCU-005 | [x] |
| TSK-DOCU-007 | Config | Agregar árbol de directorios | Insertar estructura completa del proyecto | US-DOCU-003 | documentación | `README.md` | XS | P1 | TSK-DOCU-001 | [x] |
| TSK-DOCU-008 | Config | Describir capas architecture | Explicar domain, application, infrastructure | US-DOCU-003 | documentación | `README.md` | XS | P1 | TSK-DOCU-007 | [x] |
| TSK-DOCU-009 | Config | Agregar comandos Maven | Documentar test, build, run | US-DOCU-004 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 | [x] |
| TSK-DOCU-010 | Config | Agregar comandos Docker | Documentar up, down, logs | US-DOCU-004 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 | [x] |
| TSK-DOCU-011 | Config | Crear tabla de variables | Documentar DB_URL, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION | US-DOCU-005 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 | [x] |
| TSK-DOCU-012 | Config | Agregar tabla de endpoints | Listar endpoints principales con método y descripción | US-DOCU-006 | documentación | `README.md` | XS | P2 | TSK-DOCU-001 | [x] |
| TSK-DOCU-013 | Config | Referenciar Swagger UI | Añadir enlace a documentación completa | US-DOCU-006 | documentación | `README.md` | XS | P2 | TSK-DOCU-012 | [x] |
| TSK-DOCU-014 | Config | Crear sección Contribuir | Instrucciones para branches, commits, PRs | US-DOCU-007 | documentación | `README.md` | XS | P3 | TSK-DOCU-001 | [x] |
| TSK-DOCU-015 | Config | Agregar convenciones | Describir Clean Architecture, validaciones, DI | US-DOCU-007 | documentación | `README.md` | XS | P3 | TSK-DOCU-014 | [x] |
| TSK-DOCU-016 | Test | Verificar comandos ejecutables | Probar cada comando documentado | US-DOCU-008 | documentación | - | XS | P1 | Todas las tareas | [x] |
| TSK-DOCU-017 | Test | Validar enlaces | Verificar que todos los enlaces funcionan | US-DOCU-008 | documentación | `README.md` | XS | P1 | Todas las tareas | [x] |
| TSK-DOCU-018 | Test | Verificar copy-paste | Confirmar que el código de ejemplo es ejecutable | US-DOCU-008 | documentación | `README.md` | XS | P1 | TSK-DOCU-016 | [x] |