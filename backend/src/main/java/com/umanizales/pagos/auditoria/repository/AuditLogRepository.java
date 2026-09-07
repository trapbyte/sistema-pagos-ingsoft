package com.umanizales.pagos.auditoria.repository;

import com.umanizales.pagos.auditoria.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findAllByOrderByFechaHoraDesc();

    List<AuditLog> findByAccionOrderByFechaHoraDesc(String accion);

    List<AuditLog> findByUsuarioIdOrderByFechaHoraDesc(UUID usuarioId);

    List<AuditLog> findByAccionAndUsuarioIdOrderByFechaHoraDesc(String accion, UUID usuarioId);
}
