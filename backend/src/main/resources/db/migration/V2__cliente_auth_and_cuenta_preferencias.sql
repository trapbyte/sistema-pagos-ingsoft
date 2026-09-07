ALTER TABLE cliente
    ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    ADD COLUMN rol           VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE';

ALTER TABLE cliente
    ALTER COLUMN password_hash DROP DEFAULT,
    ADD CONSTRAINT ck_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    ADD CONSTRAINT ck_cliente_rol CHECK (rol IN ('CLIENTE', 'ADMINISTRADOR'));

ALTER TABLE cuenta
    ADD COLUMN alias             VARCHAR(50),
    ADD COLUMN es_predeterminada BOOLEAN NOT NULL DEFAULT false;
