package com.umanizales.pagos.cliente.dto;

import com.umanizales.pagos.cliente.entity.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VincularCuentaRequest(
    @NotBlank String numeroCuenta,
    @NotNull TipoCuenta tipo
) {
}
