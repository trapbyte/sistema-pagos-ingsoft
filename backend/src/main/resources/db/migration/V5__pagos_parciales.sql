-- CU-21: soporte para abonos parciales, sin romper el UK de numero_referencia en factura.
ALTER TABLE factura
    ADD COLUMN monto_pagado NUMERIC(18,2) NOT NULL DEFAULT 0;

-- CU-21, FA01: la empresa debe poder rechazar pagos parciales.
ALTER TABLE empresa_servicio
    ADD COLUMN permite_pago_parcial BOOLEAN NOT NULL DEFAULT true;
