package com.umanizales.pagos.auditoria.dto;

import com.umanizales.pagos.auditoria.entity.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    Instant fechaHora,
    UUID usuarioId,
    String direccionIp,
    String accion,
    String entidadTipo,
    UUID entidadId,
    String detalle
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getFechaHora(),
            log.getUsuarioId(),
            log.getDireccionIp(),
            log.getAccion(),
            log.getEntidadTipo(),
            log.getEntidadId(),
            log.getDetalle()
        );
    }
}
