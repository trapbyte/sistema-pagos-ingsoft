package com.umanizales.pagos.cliente.dto;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.EstadoCuenta;
import com.umanizales.pagos.cliente.entity.TipoCuenta;
import com.umanizales.pagos.common.MaskUtils;

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
            MaskUtils.enmascararNumero(cuenta.getNumeroCuenta()),
            cuenta.getTipo(),
            cuenta.getEstado(),
            cuenta.getAlias(),
            cuenta.isPredeterminada(),
            cuenta.getSaldo()
        );
    }
}
