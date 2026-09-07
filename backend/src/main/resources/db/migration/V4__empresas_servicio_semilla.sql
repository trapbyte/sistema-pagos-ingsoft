-- Datos semilla de empresas de servicio para poder probar el flujo de inscripción/facturas
-- de extremo a extremo mientras no exista un CU de administración de empresas.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO empresa_servicio (id, nit, razon_social, categoria, endpoint_api) VALUES
    (gen_random_uuid(), '890000001-1', 'CHEC', 'ENERGIA', NULL),
    (gen_random_uuid(), '890000002-2', 'Efigas', 'GAS', NULL),
    (gen_random_uuid(), '890000003-3', 'Aguas de Manizales', 'ACUEDUCTO', NULL);
