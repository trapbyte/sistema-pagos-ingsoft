package com.umanizales.pagos.pago.entity;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.common.Auditable;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "domiciliacion")
@Getter
@Setter
@NoArgsConstructor
public class Domiciliacion extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_inscrito_id", nullable = false, unique = true)
    private ServicioInscrito servicioInscrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDomiciliacion estado;
}
