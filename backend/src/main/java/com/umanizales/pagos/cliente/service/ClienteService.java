package com.umanizales.pagos.cliente.service;

import com.umanizales.pagos.cliente.dto.ActualizarPerfilRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.EstadoCliente;
import com.umanizales.pagos.cliente.repository.ClienteRepository;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente obtener(UUID clienteId) {
        return clienteRepository.findById(clienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    @Transactional
    public Cliente actualizarPerfil(UUID clienteId, ActualizarPerfilRequest request) {
        Cliente cliente = obtener(clienteId);

        // CU-03, FA02: el correo no puede pertenecer a otro cliente.
        if (!cliente.getEmail().equalsIgnoreCase(request.email())
            && clienteRepository.existsByEmailAndIdNot(request.email(), clienteId)) {
            throw new DuplicateResourceException("El correo electrónico ya está en uso por otro cliente");
        }

        // El documento de identidad (CU-03, FA01) nunca se expone como editable aquí.
        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());

        return cliente;
    }

    @Transactional
    public void inactivar(UUID clienteId) {
        Cliente cliente = obtener(clienteId);
        cliente.setEstado(EstadoCliente.INACTIVO);
    }
}
