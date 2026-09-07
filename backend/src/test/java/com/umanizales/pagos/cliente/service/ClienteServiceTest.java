package com.umanizales.pagos.cliente.service;

import com.umanizales.pagos.cliente.dto.ActualizarPerfilRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.EstadoCliente;
import com.umanizales.pagos.cliente.repository.ClienteRepository;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository);
    }

    private Cliente clienteExistente() {
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setDocumentoIdentidad("123456789");
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setEmail("ana@example.com");
        cliente.setEstado(EstadoCliente.ACTIVO);
        return cliente;
    }

    @Test
    void lanzaExcepcionSiElClienteNoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtener(clienteId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizaPerfilCuandoElCorreoNoCambia() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        ActualizarPerfilRequest request = new ActualizarPerfilRequest("Ana María", "Pérez", "3009999999", "ana@example.com");
        Cliente actualizado = clienteService.actualizarPerfil(clienteId, request);

        assertThat(actualizado.getNombre()).isEqualTo("Ana María");
        assertThat(actualizado.getTelefono()).isEqualTo("3009999999");
        // El documento nunca se toca desde esta operación (CU-03, FA01).
        assertThat(actualizado.getDocumentoIdentidad()).isEqualTo("123456789");
    }

    @Test
    void rechazaActualizacionSiElNuevoCorreoYaPerteneceAOtroCliente() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmailAndIdNot("otro@example.com", clienteId)).thenReturn(true);

        ActualizarPerfilRequest request = new ActualizarPerfilRequest("Ana", "Pérez", "300", "otro@example.com");

        assertThatThrownBy(() -> clienteService.actualizarPerfil(clienteId, request))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void inactivarCambiaElEstadoDelCliente() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        clienteService.inactivar(clienteId);

        assertThat(cliente.getEstado()).isEqualTo(EstadoCliente.INACTIVO);
    }
}
