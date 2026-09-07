package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.service.CuentaService;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import com.umanizales.pagos.factura.service.FacturaService;
import com.umanizales.pagos.factura.service.ServicioInscritoService;
import com.umanizales.pagos.pago.dto.ActivarDomiciliacionRequest;
import com.umanizales.pagos.pago.entity.Domiciliacion;
import com.umanizales.pagos.pago.entity.EstadoDomiciliacion;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.repository.DomiciliacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomiciliacionServiceTest {

    @Mock
    private DomiciliacionRepository domiciliacionRepository;

    @Mock
    private ServicioInscritoService servicioInscritoService;

    @Mock
    private CuentaService cuentaService;

    @Mock
    private FacturaService facturaService;

    @Mock
    private PagoService pagoService;

    private DomiciliacionService domiciliacionService;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID servicioId = UUID.randomUUID();
    private final UUID cuentaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        domiciliacionService = new DomiciliacionService(
            domiciliacionRepository, servicioInscritoService, cuentaService, facturaService, pagoService);
    }

    private ServicioInscrito servicioInscrito() {
        ServicioInscrito servicio = new ServicioInscrito();
        servicio.setId(servicioId);
        return servicio;
    }

    private Cuenta cuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(cuentaId);
        cuenta.setNumeroCuenta("1234567890");
        return cuenta;
    }

    @Test
    void activarCreaUnaNuevaDomiciliacionSiNoExiste() {
        when(servicioInscritoService.obtenerDelCliente(clienteId, servicioId)).thenReturn(servicioInscrito());
        when(cuentaService.obtenerDelCliente(clienteId, cuentaId)).thenReturn(cuenta());
        when(domiciliacionRepository.findByServicioInscritoId(servicioId)).thenReturn(Optional.empty());
        when(domiciliacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var domiciliacion = domiciliacionService.activar(clienteId, new ActivarDomiciliacionRequest(servicioId, cuentaId));

        assertThat(domiciliacion.getEstado()).isEqualTo(EstadoDomiciliacion.ACTIVA);
        assertThat(domiciliacion.getCuenta().getId()).isEqualTo(cuentaId);
    }

    @Test
    void activarDeNuevoSobreElMismoServicioActualizaLaCuentaEnVezDeDuplicar() {
        Domiciliacion existente = new Domiciliacion();
        existente.setServicioInscrito(servicioInscrito());
        existente.setEstado(EstadoDomiciliacion.INACTIVA);

        UUID otraCuentaId = UUID.randomUUID();
        Cuenta otraCuenta = new Cuenta();
        otraCuenta.setId(otraCuentaId);
        otraCuenta.setNumeroCuenta("9999999999");

        when(servicioInscritoService.obtenerDelCliente(clienteId, servicioId)).thenReturn(servicioInscrito());
        when(cuentaService.obtenerDelCliente(clienteId, otraCuentaId)).thenReturn(otraCuenta);
        when(domiciliacionRepository.findByServicioInscritoId(servicioId)).thenReturn(Optional.of(existente));
        when(domiciliacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = domiciliacionService.activar(clienteId, new ActivarDomiciliacionRequest(servicioId, otraCuentaId));

        assertThat(resultado).isSameAs(existente);
        assertThat(resultado.getCuenta().getId()).isEqualTo(otraCuentaId);
        assertThat(resultado.getEstado()).isEqualTo(EstadoDomiciliacion.ACTIVA);
        verify(domiciliacionRepository, times(1)).save(any());
    }

    private Domiciliacion domiciliacionActiva(Factura factura) {
        Domiciliacion domiciliacion = new Domiciliacion();
        domiciliacion.setId(UUID.randomUUID());
        domiciliacion.setServicioInscrito(servicioInscrito());
        domiciliacion.setCuenta(cuenta());
        domiciliacion.setEstado(EstadoDomiciliacion.ACTIVA);
        when(facturaService.obtenerOSincronizar(domiciliacion.getServicioInscrito())).thenReturn(factura);
        return domiciliacion;
    }

    private Factura factura(LocalDate fechaVencimiento, EstadoFactura estado) {
        Factura factura = new Factura();
        factura.setEstado(estado);
        factura.setFechaVencimiento(fechaVencimiento);
        return factura;
    }

    @Test
    void ejecutaElPagoCuandoLaFacturaVenceHoyYSiguePendiente() {
        Factura factura = factura(LocalDate.now(), EstadoFactura.PENDIENTE);
        Domiciliacion domiciliacion = domiciliacionActiva(factura);
        when(domiciliacionRepository.findByEstado(EstadoDomiciliacion.ACTIVA)).thenReturn(List.of(domiciliacion));
        when(pagoService.procesarPagoAutomatico(any(), any())).thenReturn(Optional.of(new Pago()));

        domiciliacionService.ejecutarDomiciliacionesDelDia();

        verify(pagoService).procesarPagoAutomatico(domiciliacion.getCuenta(), factura);
    }

    @Test
    void noHaceNadaSiElVencimientoEsFuturo() {
        Factura factura = factura(LocalDate.now().plusDays(5), EstadoFactura.PENDIENTE);
        Domiciliacion domiciliacion = domiciliacionActiva(factura);
        when(domiciliacionRepository.findByEstado(EstadoDomiciliacion.ACTIVA)).thenReturn(List.of(domiciliacion));

        domiciliacionService.ejecutarDomiciliacionesDelDia();

        verify(pagoService, never()).procesarPagoAutomatico(any(), any());
    }

    @Test
    void noHaceNadaSiLaFacturaYaEstaPagada() {
        Factura factura = factura(LocalDate.now(), EstadoFactura.PAGADA);
        Domiciliacion domiciliacion = domiciliacionActiva(factura);
        when(domiciliacionRepository.findByEstado(EstadoDomiciliacion.ACTIVA)).thenReturn(List.of(domiciliacion));

        domiciliacionService.ejecutarDomiciliacionesDelDia();

        verify(pagoService, never()).procesarPagoAutomatico(any(), any());
    }
}
