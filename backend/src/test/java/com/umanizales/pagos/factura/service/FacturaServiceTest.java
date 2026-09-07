package com.umanizales.pagos.factura.service;

import com.umanizales.pagos.factura.entity.EmpresaServicio;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import com.umanizales.pagos.factura.gateway.EmpresaServicioGateway;
import com.umanizales.pagos.factura.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private ServicioInscritoService servicioInscritoService;

    @Mock
    private EmpresaServicioGateway empresaServicioGateway;

    private FacturaService facturaService;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID servicioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        facturaService = new FacturaService(facturaRepository, servicioInscritoService, empresaServicioGateway);
    }

    private ServicioInscrito servicioInscrito() {
        ServicioInscrito servicio = new ServicioInscrito();
        servicio.setId(servicioId);
        servicio.setNumeroReferencia("REF-001");
        servicio.setEmpresa(new EmpresaServicio());
        return servicio;
    }

    @Test
    void creaLaFacturaViaElGatewaySiNoExisteAun() {
        ServicioInscrito servicio = servicioInscrito();
        when(servicioInscritoService.obtenerDelCliente(clienteId, servicioId)).thenReturn(servicio);
        when(facturaRepository.findByNumeroReferencia("REF-001")).thenReturn(Optional.empty());
        when(empresaServicioGateway.consultarFacturaVigente(servicio.getEmpresa(), "REF-001"))
            .thenReturn(new EmpresaServicioGateway.FacturaExterna(
                new BigDecimal("75000"), LocalDate.now(), LocalDate.now().plusDays(15)));
        when(facturaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Factura factura = facturaService.obtenerFacturaVigente(clienteId, servicioId);

        assertThat(factura.getMontoTotal()).isEqualTo(new BigDecimal("75000"));
        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
    }

    @Test
    void reutilizaLaFacturaExistenteSinConsultarElGatewayDeNuevo() {
        ServicioInscrito servicio = servicioInscrito();
        Factura existente = new Factura();
        existente.setNumeroReferencia("REF-001");
        existente.setFechaVencimiento(LocalDate.now().plusDays(10));
        existente.setEstado(EstadoFactura.PENDIENTE);

        when(servicioInscritoService.obtenerDelCliente(clienteId, servicioId)).thenReturn(servicio);
        when(facturaRepository.findByNumeroReferencia("REF-001")).thenReturn(Optional.of(existente));

        Factura factura = facturaService.obtenerFacturaVigente(clienteId, servicioId);

        assertThat(factura).isSameAs(existente);
        verify(empresaServicioGateway, never()).consultarFacturaVigente(any(), any());
    }

    @Test
    void marcaComoVencidaUnaFacturaCuyaFechaLimiteYaPaso() {
        ServicioInscrito servicio = servicioInscrito();
        Factura vencida = new Factura();
        vencida.setNumeroReferencia("REF-001");
        vencida.setFechaVencimiento(LocalDate.now().minusDays(1));
        vencida.setEstado(EstadoFactura.PENDIENTE);

        when(servicioInscritoService.obtenerDelCliente(clienteId, servicioId)).thenReturn(servicio);
        when(facturaRepository.findByNumeroReferencia("REF-001")).thenReturn(Optional.of(vencida));

        Factura factura = facturaService.obtenerFacturaVigente(clienteId, servicioId);

        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.VENCIDA);
    }
}
