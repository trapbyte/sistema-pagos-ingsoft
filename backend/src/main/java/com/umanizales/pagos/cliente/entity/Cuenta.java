package com.umanizales.pagos.cliente.entity;

import com.umanizales.pagos.common.Auditable;
import com.umanizales.pagos.common.exception.BusinessRuleException;
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
import java.util.UUID;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
public class Cuenta extends Auditable {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCuenta tipo;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado;

    private String alias;

    @Column(name = "es_predeterminada", nullable = false)
    private boolean predeterminada;

    public boolean validarSaldo(BigDecimal monto) {
        return saldo.compareTo(monto) >= 0;
    }

    /**
     * CU-20/21/22: descuenta fondos de la cuenta. Lanza {@link BusinessRuleException}
     * si el saldo no alcanza (FA01 de esos casos de uso).
     */
    public void debitar(BigDecimal monto) {
        if (!validarSaldo(monto)) {
            throw new BusinessRuleException("Saldo insuficiente en la cuenta " + numeroCuenta);
        }
        this.saldo = this.saldo.subtract(monto);
    }

    public void acreditar(BigDecimal monto) {
        this.saldo = this.saldo.add(monto);
    }
}
