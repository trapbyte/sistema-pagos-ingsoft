package com.umanizales.pagos.cliente.dto;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.EstadoCuenta;
import com.umanizales.pagos.cliente.entity.TipoCuenta;

import java.math.BigDecimal;
import java.util.UUID;

public record CuentaResponse(
    UUID id,
    String numeroCuentaEnmascarado,
    TipoCuenta tipo,
    EstadoCuenta estado,
    String alias,
    boolean predeterminada,
    BigDecimal saldo
) {
    public static CuentaResponse from(Cuenta cuenta) {
        return new CuentaResponse(
            cuenta.getId(),
            enmascarar(cuenta.getNumeroCuenta()),
            cuenta.getTipo(),
            cuenta.getEstado(),
            cuenta.getAlias(),
            cuenta.isPredeterminada(),
            cuenta.getSaldo()
        );
    }

    private static String enmascarar(String numeroCuenta) {
        if (numeroCuenta.length() <= 4) {
            return numeroCuenta;
        }
        String ultimos4 = numeroCuenta.substring(numeroCuenta.length() - 4);
        return "*".repeat(numeroCuenta.length() - 4) + ultimos4;
    }
}
