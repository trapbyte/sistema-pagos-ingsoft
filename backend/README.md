# Backend — Sistema de Pagos de Servicios Públicos

Backend en **Java 21 + Spring Boot 3** para el proyecto de Ingeniería de Software. Ver el documento completo de especificación en [`../Docs/ingesoft.md`](../Docs/ingesoft.md).

## Stack

- Java 21, Spring Boot 3.3, Maven
- PostgreSQL (hosteado gratis en [Supabase](https://supabase.com))
- Flyway para migraciones de esquema
- Spring Data JPA, Spring Security, springdoc-openapi (Swagger UI)

## 1. Crear la base de datos en Supabase

1. Crea una cuenta gratuita en [supabase.com](https://supabase.com) y un nuevo proyecto (elige una región cercana, ej. `us-east-1` o `sa-east-1`).
2. Guarda la contraseña de la base de datos que te pide al crear el proyecto (o resetéala luego en **Project Settings > Database**).
3. Ve a **Project Settings > Database > Connection string** y copia el modo **Session pooler** (puerto `6543`) — se recomienda sobre la conexión directa para no agotar el límite de conexiones del plan gratuito.

## 2. Configurar variables de entorno

```bash
cp .env.example .env
# edita .env con tus datos reales de Supabase (envuelve el password en comillas simples
# si tiene caracteres especiales como $ o !, para que el shell no intente expandirlos)
set -a && source .env && set +a
```

Variables requeridas: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (ver `.env.example` para el formato exacto). Para el connection pooler de Supabase, `DB_USERNAME` debe incluir el project ref: `postgres.<project-ref>`, no solo `postgres`.

## 3. Ejecutar el backend

Con Maven instalado (o usando `mise install maven && mise use maven` si usas [mise](https://mise.jdx.dev)):

```bash
mvn spring-boot:run
```

Al iniciar, Flyway crea automáticamente el esquema (`src/main/resources/db/migration/V1__init_schema.sql`) en tu base de Supabase.

## 4. Verificar

- `GET http://localhost:8080/actuator/health` → debe responder `{"status":"UP"}` con el componente `db` en `UP`.
- `GET http://localhost:8080/swagger-ui.html` → carga la UI de Swagger (aún sin endpoints de negocio).
- En el dashboard de Supabase, **Table Editor**, deben aparecer las tablas: `cliente`, `cuenta`, `empresa_servicio`, `factura`, `pago`, `reversion`.

## Estructura del proyecto

El código está organizado por módulo de dominio (no por capa técnica), siguiendo los módulos A-D del documento de especificación:

```
com.umanizales.pagos
├── config/      Seguridad, CORS, auditoría JPA
├── common/      Clases base compartidas
├── cliente/     Módulo A: Cliente, Cuenta
├── factura/     Módulo B: EmpresaServicio, Factura
├── pago/        Módulo C: Pago
└── reversion/   Módulo D: Reversion
```

## Estado actual

Este es el esqueleto inicial: entidades JPA + esquema de base de datos + seguridad básica. La lógica de negocio (repositorios, servicios, controladores REST) se implementa módulo por módulo en las siguientes iteraciones, empezando por el Módulo A (gestión de clientes y cuentas).
