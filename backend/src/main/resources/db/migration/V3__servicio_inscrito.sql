CREATE TABLE servicio_inscrito (
    id                UUID PRIMARY KEY,
    cliente_id        UUID        NOT NULL REFERENCES cliente (id) ON DELETE RESTRICT,
    empresa_id        UUID        NOT NULL REFERENCES empresa_servicio (id) ON DELETE RESTRICT,
    numero_referencia VARCHAR(60) NOT NULL,
    alias             VARCHAR(50),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- CU-11, FA02: un mismo cliente no puede inscribir dos veces la misma referencia.
    CONSTRAINT uk_servicio_inscrito UNIQUE (cliente_id, numero_referencia)
);

CREATE INDEX idx_servicio_inscrito_cliente_id ON servicio_inscrito (cliente_id);
