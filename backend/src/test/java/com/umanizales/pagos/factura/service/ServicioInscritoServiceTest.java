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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioInscritoServiceTest {

    @Mock
    private ServicioInscritoRepository servicioInscritoRepository;

    @Mock
    private EmpresaServicioRepository empresaServicioRepository;

    @Mock
    private ClienteService clienteService;

    private ServicioInscritoService servicioInscritoService;

    private final UUID clienteId = UUID.randomUUID();
    private final UUID empresaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        servicioInscritoService = new ServicioInscritoService(
            servicioInscritoRepository, empresaServicioRepository, clienteService);
    }

    private InscribirServicioRequest requestValido() {
        return new InscribirServicioRequest(empresaId, "REF-001", "Luz Casa");
    }

    @Test
    void rechazaInscripcionDuplicadaParaElMismoCliente() {
        when(servicioInscritoRepository.existsByClienteIdAndNumeroReferencia(clienteId, "REF-001"))
            .thenReturn(true);

        assertThatThrownBy(() -> servicioInscritoService.inscribir(clienteId, requestValido()))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void permiteElMismoNumeroDeReferenciaParaDosClientesDistintos() {
        UUID otroClienteId = UUID.randomUUID();
        when(servicioInscritoRepository.existsByClienteIdAndNumeroReferencia(otroClienteId, "REF-001"))
            .thenReturn(false);
        when(empresaServicioRepository.findById(empresaId)).thenReturn(Optional.of(new EmpresaServicio()));
        when(clienteService.obtener(otroClienteId)).thenReturn(new Cliente());
        when(servicioInscritoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ServicioInscrito creado = servicioInscritoService.inscribir(otroClienteId, requestValido());

        assertThat(creado.getNumeroReferencia()).isEqualTo("REF-001");
    }

    @Test
    void rechazaInscripcionSiLaEmpresaNoExiste() {
        when(servicioInscritoRepository.existsByClienteIdAndNumeroReferencia(clienteId, "REF-001"))
            .thenReturn(false);
        when(empresaServicioRepository.findById(empresaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioInscritoService.inscribir(clienteId, requestValido()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void actualizarAliasSoloCambiaElAliasNoElNumeroDeReferencia() {
        ServicioInscrito servicio = new ServicioInscrito();
        servicio.setId(UUID.randomUUID());
        servicio.setNumeroReferencia("REF-001");
        servicio.setAlias("Alias viejo");
        when(servicioInscritoRepository.findByIdAndClienteId(servicio.getId(), clienteId))
            .thenReturn(Optional.of(servicio));

        var actualizado = servicioInscritoService.actualizarAlias(
            clienteId, servicio.getId(), new ActualizarAliasServicioRequest("Alias nuevo"));

        assertThat(actualizado.getAlias()).isEqualTo("Alias nuevo");
        assertThat(actualizado.getNumeroReferencia()).isEqualTo("REF-001");
    }

    @Test
    void lanzaExcepcionSiElServicioNoPerteneceAlCliente() {
        UUID servicioId = UUID.randomUUID();
        when(servicioInscritoRepository.findByIdAndClienteId(servicioId, clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioInscritoService.obtenerDelCliente(clienteId, servicioId))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
