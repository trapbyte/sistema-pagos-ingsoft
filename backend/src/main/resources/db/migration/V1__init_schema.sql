-- Módulo A: Clientes y Cuentas Bancarias

CREATE TABLE cliente (
    id                  UUID PRIMARY KEY,
    documento_identidad VARCHAR(30)  NOT NULL,
    tipo_documento      VARCHAR(20)  NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    telefono            VARCHAR(30),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_cliente_documento UNIQUE (documento_identidad),
    CONSTRAINT uk_cliente_email UNIQUE (email)
);

CREATE TABLE cuenta (
    id            UUID PRIMARY KEY,
    cliente_id    UUID          NOT NULL REFERENCES cliente (id) ON DELETE RESTRICT,
    numero_cuenta VARCHAR(34)   NOT NULL,
    tipo          VARCHAR(20)   NOT NULL,
    saldo         NUMERIC(18,2) NOT NULL DEFAULT 0,
    estado        VARCHAR(20)   NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_cuenta_numero UNIQUE (numero_cuenta),
    CONSTRAINT ck_cuenta_tipo CHECK (tipo IN ('AHORROS', 'CORRIENTE')),
    CONSTRAINT ck_cuenta_estado CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA'))
);

CREATE INDEX idx_cuenta_cliente_id ON cuenta (cliente_id);

-- Módulo B: Facturas y Servicios

CREATE TABLE empresa_servicio (
    id           UUID PRIMARY KEY,
    nit          VARCHAR(30)  NOT NULL,
    razon_social VARCHAR(150) NOT NULL,
    categoria    VARCHAR(50)  NOT NULL,
    endpoint_api VARCHAR(255),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_empresa_nit UNIQUE (nit)
);

CREATE TABLE factura (
    id                 UUID PRIMARY KEY,
    empresa_id         UUID          NOT NULL REFERENCES empresa_servicio (id) ON DELETE RESTRICT,
    numero_referencia  VARCHAR(60)   NOT NULL,
    monto_total        NUMERIC(18,2) NOT NULL,
    fecha_emision      DATE          NOT NULL,
    fecha_vencimiento  DATE          NOT NULL,
    estado             VARCHAR(20)   NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_factura_referencia UNIQUE (numero_referencia),
    CONSTRAINT ck_factura_estado CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA'))
);

CREATE INDEX idx_factura_empresa_id ON factura (empresa_id);

-- Módulo C: Procesamiento de Pagos

CREATE TABLE pago (
    id                  UUID PRIMARY KEY,
    cuenta_id           UUID          NOT NULL REFERENCES cuenta (id) ON DELETE RESTRICT,
    factura_id          UUID          NOT NULL REFERENCES factura (id) ON DELETE RESTRICT,
    codigo_comprobante  VARCHAR(60)   NOT NULL,
    monto               NUMERIC(18,2) NOT NULL,
    estado              VARCHAR(20)   NOT NULL,
    tipo_procesamiento  VARCHAR(20)   NOT NULL,
    fecha_hora          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uk_pago_comprobante UNIQUE (codigo_comprobante),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('PROCESANDO', 'EXITOSO', 'RECHAZADO', 'REVERSADO')),
    CONSTRAINT ck_pago_tipo_procesamiento CHECK (tipo_procesamiento IN ('MANUAL', 'DOMICILIADO'))
);

CREATE INDEX idx_pago_cuenta_id ON pago (cuenta_id);
CREATE INDEX idx_pago_factura_id ON pago (factura_id);

-- Módulo D: Operaciones Avanzadas y Soporte

CREATE TABLE reversion (
    id               UUID PRIMARY KEY,
    pago_id          UUID        NOT NULL REFERENCES pago (id) ON DELETE RESTRICT,
    fecha_solicitud  TIMESTAMPTZ NOT NULL DEFAULT now(),
    motivo           VARCHAR(500) NOT NULL,
    estado           VARCHAR(20) NOT NULL,
    CONSTRAINT uk_reversion_pago UNIQUE (pago_id),
    CONSTRAINT ck_reversion_estado CHECK (estado IN ('SOLICITADA', 'APROBADA', 'RECHAZADA', 'EJECUTADA'))
);
