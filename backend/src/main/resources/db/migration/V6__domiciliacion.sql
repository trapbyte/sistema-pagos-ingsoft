CREATE TABLE domiciliacion (
    id                   UUID PRIMARY KEY,
    servicio_inscrito_id UUID        NOT NULL UNIQUE REFERENCES servicio_inscrito (id) ON DELETE RESTRICT,
    cuenta_id            UUID        NOT NULL REFERENCES cuenta (id) ON DELETE RESTRICT,
    estado               VARCHAR(20) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- CU-23, FA01: un servicio solo puede tener una domiciliación; activarla de nuevo
    -- para otra cuenta actualiza esta misma fila en vez de crear una segunda.
    CONSTRAINT ck_domiciliacion_estado CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);

CREATE INDEX idx_domiciliacion_cuenta_id ON domiciliacion (cuenta_id);
