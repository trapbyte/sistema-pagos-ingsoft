package com.umanizales.pagos.cliente.service;

import com.umanizales.pagos.auditoria.service.AuditLogService;
import com.umanizales.pagos.cliente.dto.ActualizarPreferenciaCuentaRequest;
import com.umanizales.pagos.cliente.dto.VincularCuentaRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.EstadoCuenta;
import com.umanizales.pagos.cliente.entity.TipoCuenta;
import com.umanizales.pagos.cliente.gateway.CoreBancarioGateway;
import com.umanizales.pagos.cliente.repository.CuentaRepository;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
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
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CoreBancarioGateway coreBancarioGateway;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private AuditLogService auditLogService;

    private CuentaService cuentaService;

    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cuentaService = new CuentaService(cuentaRepository, clienteService, coreBancarioGateway, pagoRepository,
            auditLogService);
    }

    private Cuenta nuevaCuenta(boolean predeterminada) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(UUID.randomUUID());
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setPredeterminada(predeterminada);
        return cuenta;
    }

    @Test
    void primeraCuentaVinculadaQuedaComoPredeterminada() {
        VincularCuentaRequest request = new VincularCuentaRequest("1234567890", TipoCuenta.AHORROS);
        when(cuentaRepository.existsByNumeroCuenta("1234567890")).thenReturn(false);
        when(coreBancarioGateway.existeYActiva("1234567890")).thenReturn(true);
        when(clienteService.obtener(clienteId)).thenReturn(new Cliente());
        when(cuentaRepository.existsByClienteId(clienteId)).thenReturn(false);
        when(cuentaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cuenta creada = cuentaService.vincular(clienteId, request);

        assertThat(creada.isPredeterminada()).isTrue();
    }

    @Test
    void segundaCuentaVinculadaNoQuedaComoPredeterminada() {
        VincularCuentaRequest request = new VincularCuentaRequest("1234567890", TipoCuenta.AHORROS);
        when(cuentaRepository.existsByNumeroCuenta("1234567890")).thenReturn(false);
        when(coreBancarioGateway.existeYActiva("1234567890")).thenReturn(true);
        when(clienteService.obtener(clienteId)).thenReturn(new Cliente());
        when(cuentaRepository.existsByClienteId(clienteId)).thenReturn(true);
        when(cuentaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cuenta creada = cuentaService.vincular(clienteId, request);

        assertThat(creada.isPredeterminada()).isFalse();
    }

    @Test
    void rechazaVinculacionSiElCoreBancarioNoValidaLaCuenta() {
        VincularCuentaRequest request = new VincularCuentaRequest("0001234567", TipoCuenta.AHORROS);
        when(cuentaRepository.existsByNumeroCuenta("0001234567")).thenReturn(false);
        when(coreBancarioGateway.existeYActiva("0001234567")).thenReturn(false);

        assertThatThrownBy(() -> cuentaService.vincular(clienteId, request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaVinculacionSiElNumeroDeCuentaYaEstaRegistrado() {
        VincularCuentaRequest request = new VincularCuentaRequest("1234567890", TipoCuenta.AHORROS);
        when(cuentaRepository.existsByNumeroCuenta("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> cuentaService.vincular(clienteId, request))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void noPermiteDesmarcarLaUnicaCuentaPredeterminadaSinSustituta() {
        Cuenta cuenta = nuevaCuenta(true);
        when(cuentaRepository.findByIdAndClienteId(cuenta.getId(), clienteId)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.countByClienteIdAndPredeterminadaTrue(clienteId)).thenReturn(1L);

        ActualizarPreferenciaCuentaRequest request = new ActualizarPreferenciaCuentaRequest(null, false);

        assertThatThrownBy(() -> cuentaService.actualizarPreferencias(clienteId, cuenta.getId(), request))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void marcarUnaCuentaComoPredeterminadaDesmarcaLaAnterior() {
        Cuenta actualPredeterminada = nuevaCuenta(true);
        Cuenta nuevaPredeterminada = nuevaCuenta(false);
        when(cuentaRepository.findByIdAndClienteId(nuevaPredeterminada.getId(), clienteId))
            .thenReturn(Optional.of(nuevaPredeterminada));
        when(cuentaRepository.findByClienteId(clienteId))
            .thenReturn(List.of(actualPredeterminada, nuevaPredeterminada));

        ActualizarPreferenciaCuentaRequest request = new ActualizarPreferenciaCuentaRequest(null, true);
        Cuenta resultado = cuentaService.actualizarPreferencias(clienteId, nuevaPredeterminada.getId(), request);

        assertThat(resultado.isPredeterminada()).isTrue();
        assertThat(actualPredeterminada.isPredeterminada()).isFalse();
    }

    @Test
    void depositoPruebaAcreditaSaldoYRegistraAuditoria() {
        Cuenta cuenta = nuevaCuenta(true);
        cuenta.setSaldo(BigDecimal.valueOf(100));
        when(cuentaRepository.findByIdAndClienteId(cuenta.getId(), clienteId)).thenReturn(Optional.of(cuenta));

        Cuenta resultado = cuentaService.depositoPrueba(clienteId, cuenta.getId(), BigDecimal.valueOf(50));

        assertThat(resultado.getSaldo()).isEqualByComparingTo("150");
        org.mockito.Mockito.verify(auditLogService)
            .registrar(org.mockito.ArgumentMatchers.eq(clienteId), org.mockito.ArgumentMatchers.eq("DEPOSITO_PRUEBA"),
                org.mockito.ArgumentMatchers.eq("Cuenta"), org.mockito.ArgumentMatchers.eq(cuenta.getId()),
                org.mockito.ArgumentMatchers.anyString());
    }
}
