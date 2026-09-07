package com.umanizales.pagos.pago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarPagoRequest(
    @NotNull UUID cuentaId,
    @NotNull UUID facturaId,
    // Si se omite, se paga el saldo pendiente total de la factura (CU-20).
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero") BigDecimal monto
) {
}
