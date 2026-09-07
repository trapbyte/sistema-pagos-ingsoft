package com.umanizales.pagos.auditoria.controller;

import com.umanizales.pagos.auditoria.dto.AuditLogResponse;
import com.umanizales.pagos.auditoria.entity.AuditLog;
import com.umanizales.pagos.auditoria.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditLogRepository auditLogRepository;

    public AuditoriaController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> listar(
        @RequestParam(required = false) String accion,
        @RequestParam(required = false) UUID usuarioId
    ) {
        List<AuditLogResponse> logs = buscar(accion, usuarioId).stream()
            .map(AuditLogResponse::from)
            .toList();
        return ResponseEntity.ok(logs);
    }

    private List<AuditLog> buscar(String accion, UUID usuarioId) {
        if (accion != null && usuarioId != null) {
            return auditLogRepository.findByAccionAndUsuarioIdOrderByFechaHoraDesc(accion, usuarioId);
        }
        if (accion != null) {
            return auditLogRepository.findByAccionOrderByFechaHoraDesc(accion);
        }
        if (usuarioId != null) {
            return auditLogRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
        }
        return auditLogRepository.findAllByOrderByFechaHoraDesc();
    }
}
