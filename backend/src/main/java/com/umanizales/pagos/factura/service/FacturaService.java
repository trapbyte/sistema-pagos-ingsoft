package com.umanizales.pagos.factura.service;

import com.umanizales.pagos.factura.entity.EstadoFactura;
import com.umanizales.pagos.factura.entity.Factura;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import com.umanizales.pagos.factura.gateway.EmpresaServicioGateway;
import com.umanizales.pagos.factura.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ServicioInscritoService servicioInscritoService;
    private final EmpresaServicioGateway empresaServicioGateway;

    public FacturaService(FacturaRepository facturaRepository, ServicioInscritoService servicioInscritoService,
                           EmpresaServicioGateway empresaServicioGateway) {
        this.facturaRepository = facturaRepository;
        this.servicioInscritoService = servicioInscritoService;
        this.empresaServicioGateway = empresaServicioGateway;
    }

    @Transactional
    public Factura obtenerFacturaVigente(UUID clienteId, UUID servicioId) {
        ServicioInscrito servicio = servicioInscritoService.obtenerDelCliente(clienteId, servicioId);
        return obtenerOSincronizar(servicio);
    }

    /**
     * Igual que {@link #obtenerFacturaVigente}, pero sin exigir un cliente autenticado —
     * la usa el batch de domiciliaciones (CU-26), que ya tiene el {@link ServicioInscrito}
     * resuelto y no actúa en nombre de una sesión de usuario.
     */
    @Transactional
    public Factura obtenerOSincronizar(ServicioInscrito servicio) {
        Factura factura = facturaRepository.findByNumeroReferencia(servicio.getNumeroReferencia())
            .orElseGet(() -> sincronizarNuevaFactura(servicio));

        // HU-05, CU-15: si ya venció y seguía pendiente, se refleja el estado real.
        if (factura.esVencida()) {
            factura.setEstado(EstadoFactura.VENCIDA);
        }

        return factura;
    }

    private Factura sincronizarNuevaFactura(ServicioInscrito servicio) {
        EmpresaServicioGateway.FacturaExterna facturaExterna =
            empresaServicioGateway.consultarFacturaVigente(servicio.getEmpresa(), servicio.getNumeroReferencia());

        Factura factura = new Factura();
        factura.setEmpresa(servicio.getEmpresa());
        factura.setNumeroReferencia(servicio.getNumeroReferencia());
        factura.setMontoTotal(facturaExterna.montoTotal());
        factura.setFechaEmision(facturaExterna.fechaEmision());
        factura.setFechaVencimiento(facturaExterna.fechaVencimiento());
        factura.setEstado(EstadoFactura.PENDIENTE);

        return facturaRepository.save(factura);
    }
}
