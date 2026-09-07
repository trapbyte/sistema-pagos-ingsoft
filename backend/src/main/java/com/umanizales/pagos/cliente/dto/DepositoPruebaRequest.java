package com.umanizales.pagos.cliente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Solo para desarrollo/pruebas: no existe caso de uso de depósito en el documento de
 * especificación (el saldo real lo gestiona el Core Bancario, fuera de este sistema).
 * Ver {@link com.umanizales.pagos.cliente.controller.CuentaController#depositoPrueba}.
 */
public record DepositoPruebaRequest(
    @NotNull @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero") BigDecimal monto
) {
}
