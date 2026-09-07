package com.umanizales.pagos.reversion.service;

import com.umanizales.pagos.auditoria.service.AuditLogService;
import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReversionService {

    private static final long VENTANA_HORAS_REVERSION = 24;

    private final ReversionRepository reversionRepository;
    private final PagoService pagoService;
    private final AuditLogService auditLogService;

    public ReversionService(ReversionRepository reversionRepository, PagoService pagoService,
                             AuditLogService auditLogService) {
        this.reversionRepository = reversionRepository;
        this.pagoService = pagoService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Reversion solicitar(UUID clienteId, SolicitarReversionRequest request) {
        // Evita IDOR: solo se puede solicitar reversión de un pago propio.
        Pago pago = pagoService.obtenerDelCliente(clienteId, request.pagoId());

        if (pago.getEstado() != EstadoPago.EXITOSO) {
            throw new BusinessRuleException("Solo se pueden revertir pagos exitosos");
        }
        // CU-29, FA01: ventana de 24 horas desde el pago.
        if (Duration.between(pago.getFechaHora(), Instant.now()).toHours() > VENTANA_HORAS_REVERSION) {
            throw new BusinessRuleException("El plazo de 24 horas para solicitar la reversión ya venció");
        }
        // CU-29, FA02: no se permite una segunda solicitud para el mismo pago.
        if (reversionRepository.existsByPagoId(pago.getId())) {
            throw new DuplicateResourceException("Ya existe una solicitud de reversión para este pago");
        }

        Reversion reversion = new Reversion();
        reversion.setPago(pago);
        reversion.setFechaSolicitud(Instant.now());
        reversion.setMotivo(request.motivo());
        reversion.setEstado(EstadoReversion.SOLICITADA);

        return reversionRepository.save(reversion);
    }

    @Transactional(readOnly = true)
    public List<Reversion> listar(AuthenticatedUser user) {
        if (user.rol() == RolUsuario.ADMINISTRADOR) {
            return reversionRepository.findAllConPago();
        }
        return reversionRepository.findByClienteId(user.clienteId());
    }

    @Transactional(readOnly = true)
    public Reversion obtener(AuthenticatedUser user, UUID reversionId) {
        if (user.rol() == RolUsuario.ADMINISTRADOR) {
            return reversionRepository.findByIdConPago(reversionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reversión no encontrada"));
        }
        return reversionRepository.findByIdAndClienteId(reversionId, user.clienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Reversión no encontrada"));
    }

    /**
     * CU-31 (decidir) + CU-32 (reintegrar fondos), fusionados en un solo paso síncrono
     * ya que este sistema no tiene un proceso bancario asíncrono real.
     */
    @Transactional
    public Reversion decidir(UUID administradorId, UUID reversionId, DecidirReversionRequest request) {
        Reversion reversion = reversionRepository.findByIdConPago(reversionId)
            .orElseThrow(() -> new ResourceNotFoundException("Reversión no encontrada"));

        if (reversion.getEstado() != EstadoReversion.SOLICITADA) {
            throw new BusinessRuleException("Esta reversión ya fue evaluada");
        }

        reversion.setRespuestaAdministrador(request.respuesta());

        boolean aprobada = Boolean.TRUE.equals(request.aprobar());
        if (aprobada) {
            aprobarYReintegrar(reversion);
        } else {
            reversion.setEstado(EstadoReversion.RECHAZADA);
        }

        auditLogService.registrar(administradorId, aprobada ? "REVERSION_APROBADA" : "REVERSION_RECHAZADA",
            "Reversion", reversion.getId(), request.respuesta());

        return reversion;
    }

    private void aprobarYReintegrar(Reversion reversion) {
        Pago pago = reversion.getPago();
        Cuenta cuenta = pago.getCuenta();
        Factura factura = pago.getFactura();

        cuenta.acreditar(pago.getMonto());
        pago.setEstado(EstadoPago.REVERSADO);

        factura.setMontoPagado(factura.getMontoPagado().subtract(pago.getMonto()));
        // Si la factura ya estaba PAGADA, reabrirla; si era un abono parcial revertido,
        // ya seguía PENDIENTE y no hay nada más que ajustar.
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            boolean vencida = factura.getFechaVencimiento().isBefore(LocalDate.now());
            factura.setEstado(vencida ? EstadoFactura.VENCIDA : EstadoFactura.PENDIENTE);
        }

        reversion.setEstado(EstadoReversion.EJECUTADA);
    }
}
