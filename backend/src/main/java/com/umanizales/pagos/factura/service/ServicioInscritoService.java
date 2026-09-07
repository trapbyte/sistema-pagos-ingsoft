package com.umanizales.pagos.factura.service;

import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.service.ClienteService;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import com.umanizales.pagos.factura.dto.ActualizarAliasServicioRequest;
import com.umanizales.pagos.factura.dto.InscribirServicioRequest;
import com.umanizales.pagos.factura.entity.EmpresaServicio;
import com.umanizales.pagos.factura.entity.ServicioInscrito;
import com.umanizales.pagos.factura.repository.EmpresaServicioRepository;
import com.umanizales.pagos.factura.repository.ServicioInscritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServicioInscritoService {

    private final ServicioInscritoRepository servicioInscritoRepository;
    private final EmpresaServicioRepository empresaServicioRepository;
    private final ClienteService clienteService;

    public ServicioInscritoService(ServicioInscritoRepository servicioInscritoRepository,
                                    EmpresaServicioRepository empresaServicioRepository,
                                    ClienteService clienteService) {
        this.servicioInscritoRepository = servicioInscritoRepository;
        this.empresaServicioRepository = empresaServicioRepository;
        this.clienteService = clienteService;
    }

    @Transactional
    public ServicioInscrito inscribir(UUID clienteId, InscribirServicioRequest request) {
        // CU-11, FA02: no se puede inscribir dos veces el mismo contrato.
        if (servicioInscritoRepository.existsByClienteIdAndNumeroReferencia(clienteId, request.numeroReferencia())) {
            throw new DuplicateResourceException("Ya tienes este servicio inscrito");
        }

        EmpresaServicio empresa = empresaServicioRepository.findById(request.empresaId())
            .orElseThrow(() -> new ResourceNotFoundException("Empresa de servicio no encontrada"));
        Cliente cliente = clienteService.obtener(clienteId);

        ServicioInscrito servicio = new ServicioInscrito();
        servicio.setCliente(cliente);
        servicio.setEmpresa(empresa);
        servicio.setNumeroReferencia(request.numeroReferencia());
        servicio.setAlias(request.alias());

        return servicioInscritoRepository.save(servicio);
    }

    public List<ServicioInscrito> listar(UUID clienteId) {
        return servicioInscritoRepository.findByClienteId(clienteId);
    }

    public ServicioInscrito obtenerDelCliente(UUID clienteId, UUID servicioId) {
        return servicioInscritoRepository.findByIdAndClienteId(servicioId, clienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio inscrito no encontrado"));
    }

    @Transactional
    public ServicioInscrito actualizarAlias(UUID clienteId, UUID servicioId, ActualizarAliasServicioRequest request) {
        // CU-13: solo el alias es editable; el número de referencia nunca se expone aquí (FA01).
        ServicioInscrito servicio = obtenerDelCliente(clienteId, servicioId);
        servicio.setAlias(request.alias());
        return servicio;
    }

    @Transactional
    public void desinscribir(UUID clienteId, UUID servicioId) {
        ServicioInscrito servicio = obtenerDelCliente(clienteId, servicioId);
        // CU-14, FA01: la validación de domiciliación activa se añade cuando exista ese módulo.
        servicioInscritoRepository.delete(servicio);
    }
}
