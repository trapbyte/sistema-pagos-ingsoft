package com.umanizales.pagos.factura.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FacturaTest {

    private Factura facturaPendiente(String montoTotal) {
        Factura factura = new Factura();
        factura.setMontoTotal(new BigDecimal(montoTotal));
        factura.setEstado(EstadoFactura.PENDIENTE);
        return factura;
    }

    @Test
    void saldoPendienteEsElTotalCuandoNoHayAbonos() {
        Factura factura = facturaPendiente("100.00");
        assertThat(factura.saldoPendiente()).isEqualByComparingTo("100.00");
    }

    @Test
    void abonoParcialReduceElSaldoYMantienePendiente() {
        Factura factura = facturaPendiente("100.00");
        factura.marcarComoPagada(new BigDecimal("40.00"));

        assertThat(factura.saldoPendiente()).isEqualByComparingTo("60.00");
        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
    }

    @Test
    void abonoQueCubreElTotalMarcaLaFacturaComoPagada() {
        Factura factura = facturaPendiente("100.00");
        factura.marcarComoPagada(new BigDecimal("60.00"));
        factura.marcarComoPagada(new BigDecimal("40.00"));

        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.PAGADA);
        assertThat(factura.saldoPendiente()).isEqualByComparingTo("0.00");
    }
}
