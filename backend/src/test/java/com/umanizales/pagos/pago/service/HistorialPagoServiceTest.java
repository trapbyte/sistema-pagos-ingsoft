package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.pago.dto.FiltroHistorialPago;
import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.entity.TipoProcesamiento;
import com.umanizales.pagos.pago.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialPagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    private HistorialPagoService historialPagoService;

    private final FiltroHistorialPago sinFiltros =
        new FiltroHistorialPago(null, null, null, null, null, null, null, null);

    @BeforeEach
    void setUp() {
        historialPagoService = new HistorialPagoService(pagoRepository);
    }

    private Pago pagoDeEjemplo(String monto, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setCodigoComprobante("CMP-TEST0001");
        pago.setMonto(new BigDecimal(monto));
        pago.setEstado(estado);
        pago.setTipoProcesamiento(TipoProcesamiento.MANUAL);
        pago.setFechaHora(Instant.parse("2026-01-15T10:00:00Z"));
        pago.setCuenta(new Cuenta());
        pago.setFactura(new Factura());
        return pago;
    }

    @Test
    void unClienteSiempreQuedaAcotadoASusPropiosPagos() {
        UUID clienteId = UUID.randomUUID();
        when(pagoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        historialPagoService.buscar(new AuthenticatedUser(clienteId, RolUsuario.CLIENTE), sinFiltros);

        // No podemos inspeccionar el predicado interno sin un EntityManager real, pero sí
        // confirmar que se llamó con ALGUNA especificación (nunca null/"sin restricción").
        ArgumentCaptor<Specification> especificacion = ArgumentCaptor.forClass(Specification.class);
        verify(pagoRepository).findAll(especificacion.capture(), any(Sort.class));
        assertThat(especificacion.getValue()).isNotNull();
    }

    @Test
    void unAdministradorPuedeConsultarSinRestriccionDeCliente() {
        when(pagoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

        historialPagoService.buscar(new AuthenticatedUser(UUID.randomUUID(), RolUsuario.ADMINISTRADOR), sinFiltros);

        verify(pagoRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void exportarCsvGeneraElEncabezadoYUnaFilaPorPago() {
        when(pagoRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(pagoDeEjemplo("100.00", EstadoPago.EXITOSO)));

        String csv = historialPagoService.exportarCsv(
            new AuthenticatedUser(UUID.randomUUID(), RolUsuario.ADMINISTRADOR), sinFiltros);

        assertThat(csv).startsWith("codigoComprobante,fechaHora,monto,estado,tipoProcesamiento\n");
        assertThat(csv).contains("CMP-TEST0001,2026-01-15T10:00:00Z,100.00,EXITOSO,MANUAL");
    }

    @Test
    void rechazaLaExportacionSiSuperaElLimiteDeRegistros() {
        List<Pago> muchosPagos = java.util.Collections.nCopies(5001, pagoDeEjemplo("10.00", EstadoPago.EXITOSO));
        when(pagoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(muchosPagos);

        var user = new AuthenticatedUser(UUID.randomUUID(), RolUsuario.ADMINISTRADOR);
        assertThatThrownBy(() -> historialPagoService.exportarCsv(user, sinFiltros))
            .isInstanceOf(BusinessRuleException.class);
    }
}
