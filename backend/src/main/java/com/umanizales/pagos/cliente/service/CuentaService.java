package com.umanizales.pagos.cliente.service;

import com.umanizales.pagos.cliente.dto.ActualizarPreferenciaCuentaRequest;
import com.umanizales.pagos.cliente.dto.VincularCuentaRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.EstadoCuenta;
import com.umanizales.pagos.cliente.gateway.CoreBancarioGateway;
import com.umanizales.pagos.cliente.repository.CuentaRepository;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;
    private final CoreBancarioGateway coreBancarioGateway;
    private final PagoRepository pagoRepository;

    public CuentaService(CuentaRepository cuentaRepository, ClienteService clienteService,
                          CoreBancarioGateway coreBancarioGateway, PagoRepository pagoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
        this.coreBancarioGateway = coreBancarioGateway;
        this.pagoRepository = pagoRepository;
    }

    @Transactional
    public Cuenta vincular(UUID clienteId, VincularCuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.numeroCuenta())) {
            throw new DuplicateResourceException("La cuenta ya está vinculada en el sistema");
        }
        // CU-05, FA01/FA02: validar existencia y estado activo contra el Core Bancario.
        if (!coreBancarioGateway.existeYActiva(request.numeroCuenta())) {
            throw new BusinessRuleException("La cuenta no existe o no está activa en el Core Bancario");
        }

        Cliente cliente = clienteService.obtener(clienteId);
        boolean esLaPrimeraCuenta = !cuentaRepository.existsByClienteId(clienteId);

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(cliente);
        cuenta.setNumeroCuenta(request.numeroCuenta());
        cuenta.setTipo(request.tipo());
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        // CU-07: la primera cuenta vinculada queda como predeterminada automáticamente.
        cuenta.setPredeterminada(esLaPrimeraCuenta);

        return cuentaRepository.save(cuenta);
    }

    public List<Cuenta> listar(UUID clienteId) {
        return cuentaRepository.findByClienteId(clienteId);
    }

    public Cuenta obtenerDelCliente(UUID clienteId, UUID cuentaId) {
        return cuentaRepository.findByIdAndClienteId(cuentaId, clienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }

    @Transactional
    public Cuenta actualizarPreferencias(UUID clienteId, UUID cuentaId, ActualizarPreferenciaCuentaRequest request) {
        Cuenta cuenta = obtenerDelCliente(clienteId, cuentaId);

        if (request.alias() != null) {
            cuenta.setAlias(request.alias());
        }

        if (request.predeterminada() != null) {
            boolean esperada = request.predeterminada();
            // CU-07, FA01: no se puede desmarcar la única cuenta predeterminada sin elegir otra.
            if (!esperada && cuenta.isPredeterminada()
                && cuentaRepository.countByClienteIdAndPredeterminadaTrue(clienteId) <= 1) {
                throw new BusinessRuleException(
                    "Debes elegir otra cuenta predeterminada antes de desmarcar la única existente");
            }

            if (esperada && !cuenta.isPredeterminada()) {
                desmarcarPredeterminadaActual(clienteId);
            }
            cuenta.setPredeterminada(esperada);
        }

        return cuenta;
    }

    @Transactional
    public void desvincular(UUID clienteId, UUID cuentaId) {
        Cuenta cuenta = obtenerDelCliente(clienteId, cuentaId);
        // CU-08, FA01: la validación de domiciliaciones activas se añade cuando exista ese módulo.
        cuentaRepository.delete(cuenta);
    }

    public List<Pago> movimientos(UUID clienteId, UUID cuentaId) {
        Cuenta cuenta = obtenerDelCliente(clienteId, cuentaId);
        return pagoRepository.findByCuentaIdOrderByFechaHoraDesc(cuenta.getId());
    }

    private void desmarcarPredeterminadaActual(UUID clienteId) {
        cuentaRepository.findByClienteId(clienteId).stream()
            .filter(Cuenta::isPredeterminada)
            .forEach(c -> c.setPredeterminada(false));
    }
}
