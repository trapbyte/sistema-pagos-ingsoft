package com.umanizales.pagos.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * CU-36: registro inmutable de operaciones críticas del sistema. No extiende
 * {@code Auditable} (createdAt) a propósito: aquí el timestamp relevante ES el evento
 * mismo (fechaHora), no una marca de "creación de fila" incidental.
 */
@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "direccion_ip")
    private String direccionIp;

    @Column(nullable = false)
    private String accion;

    @Column(name = "entidad_tipo")
    private String entidadTipo;

    @Column(name = "entidad_id")
    private UUID entidadId;

    private String detalle;
}
