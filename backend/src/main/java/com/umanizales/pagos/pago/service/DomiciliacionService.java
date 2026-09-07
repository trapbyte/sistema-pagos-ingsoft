package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.service.CuentaService;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import com.umanizales.pagos.factura.service.FacturaService;
import com.umanizales.pagos.factura.service.ServicioInscritoService;
import com.umanizales.pagos.pago.dto.ActivarDomiciliacionRequest;
import com.umanizales.pagos.pago.dto.ActualizarDomiciliacionRequest;
import com.umanizales.pagos.pago.entity.Domiciliacion;
import com.umanizales.pagos.pago.entity.EstadoDomiciliacion;
import com.umanizales.pagos.pago.repository.DomiciliacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DomiciliacionService {

    private static final Logger logger = LoggerFactory.getLogger(DomiciliacionService.class);

    private final DomiciliacionRepository domiciliacionRepository;
    private final ServicioInscritoService servicioInscritoService;
    private final CuentaService cuentaService;
    private final FacturaService facturaService;
    private final PagoService pagoService;

    public DomiciliacionService(DomiciliacionRepository domiciliacionRepository,
                                 ServicioInscritoService servicioInscritoService,
                                 CuentaService cuentaService,
                                 FacturaService facturaService,
                                 PagoService pagoService) {
        this.domiciliacionRepository = domiciliacionRepository;
        this.servicioInscritoService = servicioInscritoService;
        this.cuentaService = cuentaService;
        this.facturaService = facturaService;
        this.pagoService = pagoService;
    }

    @Transactional
    public Domiciliacion activar(UUID clienteId, ActivarDomiciliacionRequest request) {
        ServicioInscrito servicio = servicioInscritoService.obtenerDelCliente(clienteId, request.servicioInscritoId());
        Cuenta cuenta = cuentaService.obtenerDelCliente(clienteId, request.cuentaId());

        // CU-23, FA01: si el servicio ya tiene domiciliación, se sustituye la cuenta
        // en vez de crear una segunda (el UK de la tabla lo respalda a nivel de datos).
        Domiciliacion domiciliacion = domiciliacionRepository.findByServicioInscritoId(servicio.getId())
            .orElseGet(Domiciliacion::new);
        domiciliacion.setServicioInscrito(servicio);
        domiciliacion.setCuenta(cuenta);
        domiciliacion.setEstado(EstadoDomiciliacion.ACTIVA);

        return domiciliacionRepository.save(domiciliacion);
    }

    @Transactional(readOnly = true)
    public List<Domiciliacion> listar(UUID clienteId) {
        return domiciliacionRepository.findByServicioInscritoClienteId(clienteId);
    }

    @Transactional
    public Domiciliacion actualizar(UUID clienteId, UUID domiciliacionId, ActualizarDomiciliacionRequest request) {
        Domiciliacion domiciliacion = obtenerDelCliente(clienteId, domiciliacionId);

        if (request.cuentaId() != null) {
            domiciliacion.setCuenta(cuentaService.obtenerDelCliente(clienteId, request.cuentaId()));
        }
        if (request.estado() != null) {
            domiciliacion.setEstado(request.estado());
        }

        return domiciliacion;
    }

    @Transactional(readOnly = true)
    public Domiciliacion obtenerDelCliente(UUID clienteId, UUID domiciliacionId) {
        return domiciliacionRepository.findByIdAndServicioInscritoClienteId(domiciliacionId, clienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Domiciliación no encontrada"));
    }

    /**
     * CU-26: proceso batch diario. Por cada domiciliación activa, sincroniza la factura
     * vigente de su servicio y, si vence hoy (o ya venció) y sigue pendiente, ejecuta el
     * débito automático.
     */
    @Transactional
    public void ejecutarDomiciliacionesDelDia() {
        LocalDate hoy = LocalDate.now();
        List<Domiciliacion> activas = domiciliacionRepository.findByEstado(EstadoDomiciliacion.ACTIVA);

        for (Domiciliacion domiciliacion : activas) {
            Factura factura = facturaService.obtenerOSincronizar(domiciliacion.getServicioInscrito());

            boolean estaPendiente = factura.getEstado() == EstadoFactura.PENDIENTE;
            boolean yaVenceOVencio = !factura.getFechaVencimiento().isAfter(hoy);

            if (estaPendiente && yaVenceOVencio) {
                var resultado = pagoService.procesarPagoAutomatico(domiciliacion.getCuenta(), factura);
                if (resultado.isEmpty()) {
                    logger.warn("Domiciliación {} falló por saldo insuficiente en la cuenta {}",
                        domiciliacion.getId(), domiciliacion.getCuenta().getNumeroCuenta());
                }
            }
        }
    }
}
