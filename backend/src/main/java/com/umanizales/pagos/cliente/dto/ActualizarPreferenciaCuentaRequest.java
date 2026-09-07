package com.umanizales.pagos.cliente.dto;

public record ActualizarPreferenciaCuentaRequest(
    String alias,
    Boolean predeterminada
) {
}
