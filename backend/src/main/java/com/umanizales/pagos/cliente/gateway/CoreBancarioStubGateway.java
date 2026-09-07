package com.umanizales.pagos.cliente.gateway;

import org.springframework.stereotype.Component;

/**
 * Implementación provisional: siempre valida OK, salvo números de cuenta que
 * empiecen por "000" (reservados para simular una cuenta inexistente/bloqueada
 * en pruebas manuales, ya que aún no hay Core Bancario real que consultar).
 */
@Component
public class CoreBancarioStubGateway implements CoreBancarioGateway {

    @Override
    public boolean existeYActiva(String numeroCuenta) {
        return numeroCuenta != null && !numeroCuenta.startsWith("000");
    }
}
