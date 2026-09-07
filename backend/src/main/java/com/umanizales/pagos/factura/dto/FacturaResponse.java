package com.umanizales.pagos.factura.dto;

import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FacturaResponse(
    UUID id,
    String numeroReferencia,
    BigDecimal montoTotal,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    EstadoFactura estado
) {
    public static FacturaResponse from(Factura factura) {
        return new FacturaResponse(
            factura.getId(),
            factura.getNumeroReferencia(),
            factura.getMontoTotal(),
            factura.getFechaEmision(),
            factura.getFechaVencimiento(),
            factura.getEstado()
        );
    }
}
