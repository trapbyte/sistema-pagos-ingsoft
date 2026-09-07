ALTER TABLE reversion
    ADD COLUMN respuesta_administrador VARCHAR(500);

-- Semilla de desarrollo: no existe (todavía) un mecanismo de gestión de administradores,
-- así que se siembra uno para poder probar CU-31/32. Contraseña: Admin123!
-- pgcrypto ya está habilitado desde V4.
INSERT INTO cliente (id, documento_identidad, tipo_documento, nombre, apellido, email, password_hash, estado, rol)
VALUES (
    gen_random_uuid(),
    'ADMIN-0001',
    'CC',
    'Admin',
    'Sistema',
    'admin@sistema.local',
    crypt('Admin123!', gen_salt('bf')),
    'ACTIVO',
    'ADMINISTRADOR'
);
