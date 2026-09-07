# Sistema de Pagos de Servicios Públicos

Aplicación web para inscribir, consultar y pagar facturas de servicios públicos (agua,
energía, gas, internet) desde una sola cuenta, con domiciliación automática, reversión
de pagos y auditoría de operaciones críticas.

Proyecto académico de la asignatura de Ingeniería de Software — Universidad de
Manizales. El documento de especificación completo (historias de usuario, casos de uso,
arquitectura, diagramas) está en [`Docs/ingesoft.md`](Docs/ingesoft.md).

## Stack

| Capa      | Tecnología |
|-----------|------------|
| Backend   | Java 21, Spring Boot 3.3, Spring Data JPA, Spring Security (JWT), Flyway, PDFBox |
| Frontend  | React 18, TypeScript, Vite, React Router, TanStack Query, React Hook Form + Zod |
| Base de datos | PostgreSQL (hosteado gratis en [Supabase](https://supabase.com)) |

## Estructura del repositorio

```
sistema-pagos-ingsoft/
├── Docs/ingesoft.md   Documento de especificación (requisitos, arquitectura, diagramas)
├── backend/           API REST en Spring Boot — ver backend/README.md
└── frontend/          SPA en React — ver frontend/README.md
```

## Cómo correr el proyecto

Necesitas el backend corriendo antes que el frontend, porque el frontend solo consume
su API.

### 1. Backend

```bash
cd backend
cp .env.example .env       # completa con tus credenciales de Supabase (ver backend/README.md)
set -a && source .env && set +a
mvn spring-boot:run
```

Verifica que quedó arriba en `http://localhost:8080/actuator/health` (debe responder
`{"status":"UP"}`). La primera vez que corre, Flyway crea todo el esquema automáticamente
contra tu base de Supabase. Documentación detallada, incluida la creación del proyecto en
Supabase: [`backend/README.md`](backend/README.md).

### 2. Frontend

```bash
cd frontend
cp .env.example .env       # por defecto apunta a http://localhost:8080
npm install
npm run dev
```

Abre `http://localhost:5173`. Detalles del stack y estructura:
[`frontend/README.md`](frontend/README.md).

## Perfiles disponibles

El sistema tiene dos roles (`RolUsuario`):

- **Cliente** (`CLIENTE`): el rol con el que se crea toda cuenta nueva desde
  "Crear cuenta" — no es autoasignable a administrador por seguridad. Puede vincular
  cuentas bancarias, inscribir servicios, pagar (individual, en lote o domiciliado),
  solicitar la reversión de sus propios pagos y ver su propio historial.
- **Administrador** (`ADMINISTRADOR`): no existe un flujo de registro público para este
  rol todavía (queda fuera de alcance, ver `backend/README.md`). Para probarlo hay una
  cuenta semilla creada por la migración `V7`:

  ```
  correo:     admin@sistema.local
  contraseña: Admin123!
  ```

  Un administrador puede aprobar o rechazar solicitudes de reversión de cualquier
  cliente y consultar el registro de auditoría (`/auditoria`, oculto para clientes).

Además, como este es un proyecto que solo corre en local, el perfil del cliente tiene un
botón de **"saldo de prueba"** (🧪, en Perfil → tu cuenta) para acreditar dinero
directamente sin depender de un banco real — no es un caso de uso del documento de
especificación, existe solo para poder probar pagos sin acceso a la base de datos.

## Funcionalidades

- **Clientes y cuentas** (Módulo A): registro, login (JWT), edición de perfil, dar de
  baja la cuenta; vincular/listar/desvincular cuentas bancarias, marcar una cuenta
  predeterminada, consultar saldo y movimientos.
- **Servicios y facturas** (Módulo B): inscribir/listar/editar alias/desinscribir
  servicios públicos; consultar la factura vigente de cada uno.
- **Pagos** (Módulo C): pago inmediato o parcial de una factura, pago en lote de varias
  facturas, comprobante de pago en JSON y en PDF, domiciliación (pago automático el día
  del vencimiento vía un proceso batch diario).
- **Historial y auditoría** (Módulo D): historial consolidado con filtros combinables
  (fecha, cuenta, tipo, empresa, estado, monto) y exportación a CSV; solicitud y
  decisión (aprobar/rechazar) de reversión de pagos, con reintegro de fondos; registro
  de auditoría de operaciones críticas (inicios de sesión, pagos, decisiones de
  reversión), visible solo para administradores.

Los detalles de cada caso de uso (HU-01 a HU-12, CU-01 a CU-36) están en
[`Docs/ingesoft.md`](Docs/ingesoft.md). Las integraciones con el Core Bancario y con las
Empresas de Servicio son *stubs* que simulan respuestas correctas, documentado en
`backend/README.md` junto con lo que queda explícitamente fuera de alcance (gestión real
de administradores, notificaciones por correo, exportación de reportes en PDF).
