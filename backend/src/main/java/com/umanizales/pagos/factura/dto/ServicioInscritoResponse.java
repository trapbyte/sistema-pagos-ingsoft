package com.umanizales.pagos.factura.dto;

import com.umanizales.pagos.factura.entity.ServicioInscrito;

import java.util.UUID;

public record ServicioInscritoResponse(
    UUID id,
    UUID empresaId,
    String empresaRazonSocial,
    String numeroReferencia,
    String alias
) {
    public static ServicioInscritoResponse from(ServicioInscrito servicio) {
        return new ServicioInscritoResponse(
            servicio.getId(),
            servicio.getEmpresa().getId(),
            servicio.getEmpresa().getRazonSocial(),
            servicio.getNumeroReferencia(),
            servicio.getAlias()
        );
    }
}
