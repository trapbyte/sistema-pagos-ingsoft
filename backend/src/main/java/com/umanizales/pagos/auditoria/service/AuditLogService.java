package com.umanizales.pagos.auditoria.service;

import com.umanizales.pagos.auditoria.entity.AuditLog;
import com.umanizales.pagos.auditoria.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

/**
 * CU-36: registra operaciones críticas. Se llama siempre DENTRO de la transacción de la
 * operación que audita (nunca en una transacción separada) — así, si el guardado del log
 * falla, la operación completa se revierte (CU-36, FA01: "la transacción crítica se detiene").
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void registrar(UUID usuarioId, String accion, String entidadTipo, UUID entidadId, String detalle) {
        auditLogRepository.save(construir(usuarioId, accion, entidadTipo, entidadId, detalle));
    }

    /**
     * Igual que {@link #registrar}, pero en su propia transacción independiente
     * (commit inmediato). Se usa para auditar el FRACASO de una operación (ej. un login
     * fallido): si se guardara en la misma transacción, el rollback que sigue al lanzar
     * la excepción de negocio borraría también el registro de auditoría que queremos
     * conservar.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEnNuevaTransaccion(UUID usuarioId, String accion, String entidadTipo, UUID entidadId,
                                             String detalle) {
        auditLogRepository.save(construir(usuarioId, accion, entidadTipo, entidadId, detalle));
    }

    private AuditLog construir(UUID usuarioId, String accion, String entidadTipo, UUID entidadId, String detalle) {
        AuditLog log = new AuditLog();
        log.setFechaHora(Instant.now());
        log.setUsuarioId(usuarioId);
        log.setDireccionIp(obtenerIpDeLaPeticionActual());
        log.setAccion(accion);
        log.setEntidadTipo(entidadTipo);
        log.setEntidadId(entidadId);
        log.setDetalle(detalle);
        return log;
    }

    /**
     * Devuelve null si no hay una petición HTTP en curso (ej. el batch de domiciliaciones,
     * que se dispara por cron sin request asociado).
     */
    private String obtenerIpDeLaPeticionActual() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }
}
