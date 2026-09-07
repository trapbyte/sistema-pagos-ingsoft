package com.umanizales.pagos.pago.dto;

import com.umanizales.pagos.cliente.entity.TipoCuenta;
import com.umanizales.pagos.pago.entity.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CU-34: todos los campos son opcionales (null = "sin filtrar por esto").
 */
public record FiltroHistorialPago(
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    UUID cuentaId,
    TipoCuenta tipoCuenta,
    UUID empresaId,
    EstadoPago estado,
    BigDecimal montoMinimo,
    BigDecimal montoMaximo
) {
}
