package com.umanizales.pagos.pago.dto;

import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.entity.TipoProcesamiento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagoResumenResponse(
    UUID id,
    String codigoComprobante,
    BigDecimal monto,
    EstadoPago estado,
    TipoProcesamiento tipoProcesamiento,
    Instant fechaHora
) {
    public static PagoResumenResponse from(Pago pago) {
        return new PagoResumenResponse(
            pago.getId(),
            pago.getCodigoComprobante(),
            pago.getMonto(),
            pago.getEstado(),
            pago.getTipoProcesamiento(),
            pago.getFechaHora()
        );
    }
}
