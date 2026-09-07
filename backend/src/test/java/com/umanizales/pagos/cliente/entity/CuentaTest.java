package com.umanizales.pagos.cliente.entity;

import com.umanizales.pagos.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private Cuenta cuentaConSaldo(String saldo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setSaldo(new BigDecimal(saldo));
        return cuenta;
    }

    @Test
    void validarSaldoEsVerdaderoCuandoElSaldoAlcanza() {
        Cuenta cuenta = cuentaConSaldo("100.00");
        assertThat(cuenta.validarSaldo(new BigDecimal("100.00"))).isTrue();
        assertThat(cuenta.validarSaldo(new BigDecimal("100.01"))).isFalse();
    }

    @Test
    void debitarDescuentaElSaldoCuandoAlcanza() {
        Cuenta cuenta = cuentaConSaldo("100.00");
        cuenta.debitar(new BigDecimal("40.00"));
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("60.00");
    }

    @Test
    void debitarLanzaExcepcionSiElSaldoNoAlcanza() {
        Cuenta cuenta = cuentaConSaldo("50.00");
        assertThatThrownBy(() -> cuenta.debitar(new BigDecimal("50.01")))
            .isInstanceOf(BusinessRuleException.class);
        // El saldo no debe modificarse si la operación falla.
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    void acreditarSumaAlSaldo() {
        Cuenta cuenta = cuentaConSaldo("50.00");
        cuenta.acreditar(new BigDecimal("25.00"));
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("75.00");
    }
}
