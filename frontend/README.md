# Frontend — Sistema de Pagos de Servicios Públicos

SPA en **React 18 + TypeScript + Vite** que consume el backend en
[`../backend`](../backend). Ver el documento de especificación en
[`../Docs/ingesoft.md`](../Docs/ingesoft.md).

## Stack

- React 18, TypeScript, Vite
- React Router v6 (rutas protegidas por sesión/rol)
- TanStack Query (cache de datos del servidor)
- React Hook Form + Zod (formularios y validación, alineada a las reglas del backend)
- lucide-react (iconos)
- CSS plano con variables + CSS Modules (sin framework de UI) — el sistema de diseño
  vive en `src/styles/tokens.css`

## Ejecutar en desarrollo

```bash
cp .env.example .env   # VITE_API_URL apunta al backend, por defecto http://localhost:8080
npm install
npm run dev
```

El backend debe estar corriendo en paralelo (ver `../backend/README.md`). CORS ya está
habilitado en el backend para cualquier origen `http://localhost:*`, así que no hace
falta configurar nada del lado del servidor para el dev server de Vite.

## Estructura

```
src/
├── auth/        Contexto de sesión (JWT en localStorage) y guards de ruta
├── components/  UI compartida: AppShell, Button, Field, Card/BillStub, Badge, Modal, Toast
├── features/    Una carpeta por módulo de dominio (auth, dashboard, cuentas vía perfil,
│                servicios, pagos, domiciliacion, reversiones, auditoria, perfil)
├── hooks/       Hooks de TanStack Query por recurso de la API
├── lib/         apiClient (fetch + JWT + manejo de errores), format, schemas de Zod
├── styles/      tokens.css (paleta, tipografía, espaciado) y global.css (reset/base)
└── types/api.ts Tipos espejo de los DTOs/enums del backend — mantener sincronizado a mano
```

## Diseño

Mobile-first: navegación inferior de pestañas en móvil, sidebar en pantallas ≥960px.
Paleta y tipografía (Space Grotesk + Inter) definidas como variables CSS en
`src/styles/tokens.css`, con soporte de modo oscuro vía `prefers-color-scheme`. El
color de cada categoría de servicio (agua/energía/gas/internet) es información visual
consistente en toda la app (`src/lib/serviceCategory.ts`).

## Estado actual

Cubre las pantallas de los 4 módulos del backend: autenticación, cuentas y perfil,
servicios/facturas, pago individual y en lote con comprobante (JSON + PDF), historial
con filtros y exportación CSV, domiciliación, reversiones (solicitud y decisión con rol
Administrador) y auditoría (solo Administrador). Verificado manualmente end-to-end
contra el backend real (Supabase).

Fuera de alcance (documentado también en el backend): gestión de administradores desde
la UI, notificaciones por correo.
