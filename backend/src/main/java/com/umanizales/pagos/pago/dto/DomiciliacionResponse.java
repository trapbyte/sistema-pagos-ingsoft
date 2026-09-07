package com.umanizales.pagos.pago.dto;

import com.umanizales.pagos.common.MaskUtils;
import com.umanizales.pagos.pago.entity.Domiciliacion;
import com.umanizales.pagos.pago.entity.EstadoDomiciliacion;

import java.util.UUID;

public record DomiciliacionResponse(
    UUID id,
    UUID servicioInscritoId,
    String numeroReferencia,
    UUID cuentaId,
    String cuentaEnmascarada,
    EstadoDomiciliacion estado
) {
    public static DomiciliacionResponse from(Domiciliacion domiciliacion) {
        return new DomiciliacionResponse(
            domiciliacion.getId(),
            domiciliacion.getServicioInscrito().getId(),
            domiciliacion.getServicioInscrito().getNumeroReferencia(),
            domiciliacion.getCuenta().getId(),
            MaskUtils.enmascararNumero(domiciliacion.getCuenta().getNumeroCuenta()),
            domiciliacion.getEstado()
        );
    }
}
