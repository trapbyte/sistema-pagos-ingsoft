package com.umanizales.pagos.reversion.service;

import com.umanizales.pagos.auditoria.service.AuditLogService;
import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.service.PagoService;
import com.umanizales.pagos.reversion.dto.DecidirReversionRequest;
import com.umanizales.pagos.reversion.dto.SolicitarReversionRequest;
import com.umanizales.pagos.reversion.entity.EstadoReversion;
import com.umanizales.pagos.reversion.entity.Reversion;
import com.umanizales.pagos.reversion.repository.ReversionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReversionServiceTest {

    @Mock
    private ReversionRepository reversionRepository;

    @Mock
    private PagoService pagoService;

    @Mock
    private AuditLogService auditLogService;

    private ReversionService reversionService;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID pagoId = UUID.randomUUID();
    private final UUID administradorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        reversionService = new ReversionService(reversionRepository, pagoService, auditLogService);
    }

    private Pago pagoExitoso(Instant fechaHora) {
        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(new BigDecimal("100.00"));

        Factura factura = new Factura();
        factura.setMontoTotal(new BigDecimal("50.00"));
        factura.setEstado(EstadoFactura.PAGADA);
        factura.setFechaVencimiento(LocalDate.now().plusDays(10));
        factura.marcarComoPagada(new BigDecimal("50.00"));

        Pago pago = new Pago();
        pago.setId(pagoId);
        pago.setCuenta(cuenta);
        pago.setFactura(factura);
        pago.setMonto(new BigDecimal("50.00"));
        pago.setEstado(EstadoPago.EXITOSO);
        pago.setFechaHora(fechaHora);
        return pago;
    }

    @Test
    void rechazaSolicitudSiElPagoNoEstaExitoso() {
        Pago pago = pagoExitoso(Instant.now());
        pago.setEstado(EstadoPago.RECHAZADO);
        when(pagoService.obtenerDelCliente(clienteId, pagoId)).thenReturn(pago);

        var request = new SolicitarReversionRequest(pagoId, "Cobro duplicado");
        assertThatThrownBy(() -> reversionService.solicitar(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaSolicitudFueraDeLaVentanaDe24Horas() {
        Pago pago = pagoExitoso(Instant.now().minus(java.time.Duration.ofHours(25)));
        when(pagoService.obtenerDelCliente(clienteId, pagoId)).thenReturn(pago);

        var request = new SolicitarReversionRequest(pagoId, "Cobro duplicado");
        assertThatThrownBy(() -> reversionService.solicitar(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaSiYaExisteUnaSolicitudParaElMismoPago() {
        Pago pago = pagoExitoso(Instant.now());
        when(pagoService.obtenerDelCliente(clienteId, pagoId)).thenReturn(pago);
        when(reversionRepository.existsByPagoId(pagoId)).thenReturn(true);

        var request = new SolicitarReversionRequest(pagoId, "Cobro duplicado");
        assertThatThrownBy(() -> reversionService.solicitar(clienteId, request))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void solicitudValidaQuedaEnEstadoSolicitada() {
        Pago pago = pagoExitoso(Instant.now());
        when(pagoService.obtenerDelCliente(clienteId, pagoId)).thenReturn(pago);
        when(reversionRepository.existsByPagoId(pagoId)).thenReturn(false);
        when(reversionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var reversion = reversionService.solicitar(clienteId, new SolicitarReversionRequest(pagoId, "Cobro duplicado"));

        assertThat(reversion.getEstado()).isEqualTo(EstadoReversion.SOLICITADA);
        assertThat(reversion.getPago()).isEqualTo(pago);
    }

    private Reversion reversionSolicitada(Pago pago) {
        Reversion reversion = new Reversion();
        reversion.setId(UUID.randomUUID());
        reversion.setPago(pago);
        reversion.setEstado(EstadoReversion.SOLICITADA);
        return reversion;
    }

    @Test
    void aprobarAcreditaLaCuentaYReabreLaFactura() {
        Pago pago = pagoExitoso(Instant.now());
        Reversion reversion = reversionSolicitada(pago);
        when(reversionRepository.findByIdConPago(reversion.getId())).thenReturn(Optional.of(reversion));

        var resultado = reversionService.decidir(administradorId, reversion.getId(), new DecidirReversionRequest(true, "Procede"));

        assertThat(resultado.getEstado()).isEqualTo(EstadoReversion.EJECUTADA);
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.REVERSADO);
        assertThat(pago.getCuenta().getSaldo()).isEqualByComparingTo("150.00");
        assertThat(pago.getFactura().getEstado()).isEqualTo(EstadoFactura.PENDIENTE);
        assertThat(resultado.getRespuestaAdministrador()).isEqualTo("Procede");
        org.mockito.Mockito.verify(auditLogService).registrar(administradorId, "REVERSION_APROBADA",
            "Reversion", reversion.getId(), "Procede");
    }

    @Test
    void rechazarDejaElPagoIntacto() {
        Pago pago = pagoExitoso(Instant.now());
        Reversion reversion = reversionSolicitada(pago);
        when(reversionRepository.findByIdConPago(reversion.getId())).thenReturn(Optional.of(reversion));

        var resultado = reversionService.decidir(administradorId, reversion.getId(), new DecidirReversionRequest(false, "No procede"));

        assertThat(resultado.getEstado()).isEqualTo(EstadoReversion.RECHAZADA);
        assertThat(pago.getEstado()).isEqualTo(EstadoPago.EXITOSO);
        assertThat(pago.getCuenta().getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void rechazaDecidirSobreUnaReversionYaEvaluada() {
        Pago pago = pagoExitoso(Instant.now());
        Reversion reversion = reversionSolicitada(pago);
        reversion.setEstado(EstadoReversion.RECHAZADA);
        when(reversionRepository.findByIdConPago(reversion.getId())).thenReturn(Optional.of(reversion));

        var request = new DecidirReversionRequest(true, "Procede");
        assertThatThrownBy(() -> reversionService.decidir(administradorId, reversion.getId(), request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void listarDevuelveTodasParaUnAdministrador() {
        reversionService.listar(new AuthenticatedUser(UUID.randomUUID(), RolUsuario.ADMINISTRADOR));
        org.mockito.Mockito.verify(reversionRepository).findAllConPago();
    }

    @Test
    void listarDevuelveSoloLasPropiasParaUnCliente() {
        AuthenticatedUser user = new AuthenticatedUser(clienteId, RolUsuario.CLIENTE);
        reversionService.listar(user);
        org.mockito.Mockito.verify(reversionRepository).findByClienteId(clienteId);
    }
}
