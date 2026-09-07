package com.umanizales.pagos.cliente.gateway;

/**
 * Puerto hacia el Core Bancario externo (ver sección 3.1 del documento de especificación).
 * La implementación real (HTTP/TLS contra el core) se añade en una iteración posterior;
 * por ahora solo existe {@link CoreBancarioStubGateway} para no bloquear el flujo de negocio.
 */
public interface CoreBancarioGateway {

    /**
     * Verifica que el número de cuenta exista en el core bancario y no esté
     * embargada/inactiva/bloqueada (CU-05, FA01/FA02).
     */
    boolean existeYActiva(String numeroCuenta);
}
