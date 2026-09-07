package com.umanizales.pagos.factura.entity;

import com.umanizales.pagos.common.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
public class Factura extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaServicio empresa;

    @Column(name = "numero_referencia", nullable = false, unique = true)
    private String numeroReferencia;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    @Column(name = "monto_pagado", nullable = false)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    public boolean esVencida() {
        return estado != EstadoFactura.PAGADA
            && estado != EstadoFactura.ANULADA
            && fechaVencimiento.isBefore(LocalDate.now());
    }

    public BigDecimal saldoPendiente() {
        return montoTotal.subtract(montoPagado);
    }

    /**
     * Aplica un abono (CU-20/CU-21): acumula lo pagado y, si cubre el total,
     * marca la factura como {@link EstadoFactura#PAGADA}.
     */
    public void marcarComoPagada(BigDecimal monto) {
        this.montoPagado = this.montoPagado.add(monto);
        if (this.montoPagado.compareTo(this.montoTotal) >= 0) {
            this.estado = EstadoFactura.PAGADA;
        }
    }
}
