CREATE TABLE auditoria (
    id            UUID PRIMARY KEY,
    fecha_hora    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Nulo cuando la acción la dispara un proceso automático (ej. batch de domiciliaciones)
    -- sin un usuario autenticado detrás.
    usuario_id    UUID REFERENCES cliente (id) ON DELETE SET NULL,
    direccion_ip  VARCHAR(45),
    accion        VARCHAR(100) NOT NULL,
    entidad_tipo  VARCHAR(50),
    entidad_id    UUID,
    detalle       VARCHAR(1000)
);

CREATE INDEX idx_auditoria_usuario_id ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_fecha_hora ON auditoria (fecha_hora);
