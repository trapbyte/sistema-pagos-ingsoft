package com.umanizales.pagos.reversion.dto;

import com.umanizales.pagos.reversion.entity.EstadoReversion;
import com.umanizales.pagos.reversion.entity.Reversion;

import java.time.Instant;
import java.util.UUID;

public record ReversionResponse(
    UUID id,
    UUID pagoId,
    String codigoComprobantePago,
    Instant fechaSolicitud,
    String motivo,
    EstadoReversion estado,
    String respuestaAdministrador
) {
    public static ReversionResponse from(Reversion reversion) {
        return new ReversionResponse(
            reversion.getId(),
            reversion.getPago().getId(),
            reversion.getPago().getCodigoComprobante(),
            reversion.getFechaSolicitud(),
            reversion.getMotivo(),
            reversion.getEstado(),
            reversion.getRespuestaAdministrador()
        );
    }
}
