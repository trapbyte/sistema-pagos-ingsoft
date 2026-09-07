package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.service.CuentaService;
import com.umanizales.pagos.common.MaskUtils;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.repository.FacturaRepository;
import com.umanizales.pagos.pago.dto.ComprobanteResponse;
import com.umanizales.pagos.pago.dto.RegistrarPagoLoteRequest;
import com.umanizales.pagos.pago.dto.RegistrarPagoRequest;
import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.entity.TipoProcesamiento;
import com.umanizales.pagos.pago.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final CuentaService cuentaService;

    public PagoService(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                        CuentaService cuentaService) {
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
        this.cuentaService = cuentaService;
    }

    @Transactional
    public Pago registrarPago(UUID clienteId, RegistrarPagoRequest request) {
        Cuenta cuenta = cuentaService.obtenerDelCliente(clienteId, request.cuentaId());
        Factura factura = obtenerFacturaPagable(request.facturaId());

        BigDecimal monto = request.monto() != null ? request.monto() : factura.saldoPendiente();
        validarMontoDelAbono(factura, monto);

        return ejecutarPago(cuenta, factura, monto, TipoProcesamiento.MANUAL);
    }

    @Transactional
    public List<Pago> registrarPagoLote(UUID clienteId, RegistrarPagoLoteRequest request) {
        Cuenta cuenta = cuentaService.obtenerDelCliente(clienteId, request.cuentaId());

        List<Factura> facturas = request.facturaIds().stream()
            .map(this::obtenerFacturaPagable)
            .toList();

        // CU-22, FA01: todo o nada — se valida el total antes de debitar cualquier factura.
        BigDecimal totalRequerido = facturas.stream()
            .map(Factura::saldoPendiente)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!cuenta.validarSaldo(totalRequerido)) {
            throw new BusinessRuleException(
                "El saldo de la cuenta no alcanza para cubrir el total del lote seleccionado");
        }

        return facturas.stream()
            .map(factura -> ejecutarPago(cuenta, factura, factura.saldoPendiente(), TipoProcesamiento.MANUAL))
            .toList();
    }

    @Transactional(readOnly = true)
    public Pago obtenerDelCliente(UUID clienteId, UUID pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        // Evita IDOR: el pago solo es visible si la cuenta que debitó pertenece al cliente.
        cuentaService.obtenerDelCliente(clienteId, pago.getCuenta().getId());
        return pago;
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse generarComprobante(UUID clienteId, UUID pagoId) {
        Pago pago = obtenerDelCliente(clienteId, pagoId);
        // CU-27, FA01: sin comprobante definitivo si el pago no quedó exitoso.
        if (pago.getEstado() != EstadoPago.EXITOSO) {
            throw new BusinessRuleException("Solo los pagos exitosos generan comprobante definitivo");
        }

        Factura factura = pago.getFactura();
        Cuenta cuenta = pago.getCuenta();
        return new ComprobanteResponse(
            pago.getId(),
            pago.getCodigoComprobante(),
            cuenta.getCliente().getNombre() + " " + cuenta.getCliente().getApellido(),
            MaskUtils.enmascararNumero(cuenta.getNumeroCuenta()),
            factura.getEmpresa().getRazonSocial(),
            factura.getNumeroReferencia(),
            pago.getMonto(),
            pago.getFechaHora()
        );
    }

    private Factura obtenerFacturaPagable(UUID facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
        if (factura.getEstado() == EstadoFactura.PAGADA || factura.getEstado() == EstadoFactura.ANULADA) {
            throw new BusinessRuleException("La factura no admite pagos en su estado actual: " + factura.getEstado());
        }
        return factura;
    }

    private void validarMontoDelAbono(Factura factura, BigDecimal monto) {
        if (monto.compareTo(factura.saldoPendiente()) > 0) {
            throw new BusinessRuleException("El monto supera el saldo pendiente de la factura");
        }
        // CU-21, FA01: la empresa debe permitir abonos parciales.
        boolean esAbonoParcial = monto.compareTo(factura.saldoPendiente()) < 0;
        if (esAbonoParcial && !factura.getEmpresa().isPermitePagoParcial()) {
            throw new BusinessRuleException("Esta empresa no admite pagos parciales; debes pagar el total");
        }
    }

    private Pago ejecutarPago(Cuenta cuenta, Factura factura, BigDecimal monto, TipoProcesamiento tipo) {
        cuenta.debitar(monto);
        factura.marcarComoPagada(monto);

        Pago pago = new Pago();
        pago.setCuenta(cuenta);
        pago.setFactura(factura);
        pago.setCodigoComprobante(generarCodigoComprobante());
        pago.setMonto(monto);
        pago.setEstado(EstadoPago.EXITOSO);
        pago.setTipoProcesamiento(tipo);
        pago.setFechaHora(Instant.now());

        return pagoRepository.save(pago);
    }

    private String generarCodigoComprobante() {
        return "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
