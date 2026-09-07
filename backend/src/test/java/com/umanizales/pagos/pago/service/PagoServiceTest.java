package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.service.CuentaService;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.factura.entity.EmpresaServicio;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.repository.FacturaRepository;
import com.umanizales.pagos.pago.dto.RegistrarPagoLoteRequest;
import com.umanizales.pagos.pago.dto.RegistrarPagoRequest;
import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private CuentaService cuentaService;

    private PagoService pagoService;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID cuentaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        pagoService = new PagoService(pagoRepository, facturaRepository, cuentaService);
    }

    private Cuenta cuentaConSaldo(String saldo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(cuentaId);
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setSaldo(new BigDecimal(saldo));
        return cuenta;
    }

    private Factura facturaPendiente(String montoTotal, boolean permitePagoParcial) {
        EmpresaServicio empresa = new EmpresaServicio();
        empresa.setPermitePagoParcial(permitePagoParcial);

        Factura factura = new Factura();
        factura.setId(UUID.randomUUID());
        factura.setEmpresa(empresa);
        factura.setMontoTotal(new BigDecimal(montoTotal));
        factura.setEstado(EstadoFactura.PENDIENTE);
        return factura;
    }

    @Test
    void pagoTotalDebitaLaCuentaYMarcaLaFacturaComoPagada() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura = facturaPendiente("100.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));
        when(pagoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pago pago = pagoService.registrarPago(clienteId, new RegistrarPagoRequest(cuentaId, factura.getId(), null));

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.EXITOSO);
        assertThat(pago.getMonto()).isEqualByComparingTo("100.00");
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("400.00");
        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.PAGADA);
    }

    @Test
    void pagoParcialAcumulaMontoPagadoYMantienePendiente() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura = facturaPendiente("100.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));
        when(pagoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        pagoService.registrarPago(clienteId, new RegistrarPagoRequest(cuentaId, factura.getId(), new BigDecimal("40.00")));

        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
        assertThat(factura.saldoPendiente()).isEqualByComparingTo("60.00");
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("460.00");
    }

    @Test
    void rechazaPagoParcialSiLaEmpresaNoLoPermite() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura = facturaPendiente("100.00", false);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));

        var request = new RegistrarPagoRequest(cuentaId, factura.getId(), new BigDecimal("40.00"));
        assertThatThrownBy(() -> pagoService.registrarPago(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaPagoSiElMontoSuperaElSaldoPendienteDeLaFactura() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura = facturaPendiente("100.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));

        var request = new RegistrarPagoRequest(cuentaId, factura.getId(), new BigDecimal("150.00"));
        assertThatThrownBy(() -> pagoService.registrarPago(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaPagoSiElSaldoDeLaCuentaEsInsuficiente() {
        Cuenta cuenta = cuentaConSaldo("50.00");
        Factura factura = facturaPendiente("100.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));

        var request = new RegistrarPagoRequest(cuentaId, factura.getId(), null);
        assertThatThrownBy(() -> pagoService.registrarPago(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaPagoDeFacturaYaPagada() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura = facturaPendiente("100.00", true);
        factura.setEstado(EstadoFactura.PAGADA);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura.getId())).thenReturn(Optional.of(factura));

        var request = new RegistrarPagoRequest(cuentaId, factura.getId(), null);
        assertThatThrownBy(() -> pagoService.registrarPago(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void loteNoDebitaNadaSiElTotalExcedeElSaldo() {
        Cuenta cuenta = cuentaConSaldo("100.00");
        Factura factura1 = facturaPendiente("60.00", true);
        Factura factura2 = facturaPendiente("60.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura1.getId())).thenReturn(Optional.of(factura1));
        when(facturaRepository.findById(factura2.getId())).thenReturn(Optional.of(factura2));

        var request = new RegistrarPagoLoteRequest(cuentaId, List.of(factura1.getId(), factura2.getId()));
        assertThatThrownBy(() -> pagoService.registrarPagoLote(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);

        // Todo o nada: ninguna factura debió modificarse.
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.00");
        assertThat(factura1.getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
        assertThat(factura2.getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
    }

    @Test
    void loteExitosoPagaTodasLasFacturasSeleccionadas() {
        Cuenta cuenta = cuentaConSaldo("200.00");
        Factura factura1 = facturaPendiente("60.00", true);
        Factura factura2 = facturaPendiente("40.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura1.getId())).thenReturn(Optional.of(factura1));
        when(facturaRepository.findById(factura2.getId())).thenReturn(Optional.of(factura2));
        when(pagoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new RegistrarPagoLoteRequest(cuentaId, List.of(factura1.getId(), factura2.getId()));
        List<Pago> pagos = pagoService.registrarPagoLote(clienteId, request);

        assertThat(pagos).hasSize(2);
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.00");
        assertThat(factura1.getEstado()).isEqualTo(EstadoFactura.PAGADA);
        assertThat(factura2.getEstado()).isEqualTo(EstadoFactura.PAGADA);
    }

    @Test
    void generaCodigoDeComprobanteUnicoPorPago() {
        Cuenta cuenta = cuentaConSaldo("500.00");
        Factura factura1 = facturaPendiente("50.00", true);
        Factura factura2 = facturaPendiente("50.00", true);
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta);
        when(facturaRepository.findById(factura1.getId())).thenReturn(Optional.of(factura1));
        when(facturaRepository.findById(factura2.getId())).thenReturn(Optional.of(factura2));
        when(pagoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pago pago1 = pagoService.registrarPago(clienteId, new RegistrarPagoRequest(cuentaId, factura1.getId(), null));
        Pago pago2 = pagoService.registrarPago(clienteId, new RegistrarPagoRequest(cuentaId, factura2.getId(), null));

        assertThat(pago1.getCodigoComprobante()).isNotEqualTo(pago2.getCodigoComprobante());
    }
}
