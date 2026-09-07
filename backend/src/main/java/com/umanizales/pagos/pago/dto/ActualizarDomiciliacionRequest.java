package com.umanizales.pagos.pago.dto;

import com.umanizales.pagos.pago.entity.EstadoDomiciliacion;

import java.util.UUID;

public record ActualizarDomiciliacionRequest(
    UUID cuentaId,
    EstadoDomiciliacion estado
) {
}
