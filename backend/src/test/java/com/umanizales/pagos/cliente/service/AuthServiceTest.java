package com.umanizales.pagos.cliente.service;

import com.umanizales.pagos.cliente.dto.LoginRequest;
import com.umanizales.pagos.cliente.dto.LoginResponse;
import com.umanizales.pagos.cliente.dto.RegistroClienteRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.EstadoCliente;
import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.cliente.repository.ClienteRepository;
import com.umanizales.pagos.auditoria.service.AuditLogService;
import com.umanizales.pagos.common.exception.DuplicateResourceException;
import com.umanizales.pagos.common.exception.InvalidCredentialsException;
import com.umanizales.pagos.config.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(clienteRepository, passwordEncoder, jwtService, auditLogService);
    }

    private RegistroClienteRequest registroValido() {
        return new RegistroClienteRequest(
            "123456789", "CC", "Ana", "Pérez", "ana@example.com", "3001234567", "Segura123"
        );
    }

    @Test
    void rechazaRegistroConDocumentoDuplicado() {
        when(clienteRepository.existsByDocumentoIdentidad("123456789")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(registroValido()))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void rechazaRegistroConEmailDuplicado() {
        when(clienteRepository.existsByDocumentoIdentidad(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(registroValido()))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registraClienteConPasswordHasheadoYRolClientePorDefecto() {
        when(clienteRepository.existsByDocumentoIdentidad(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Segura123")).thenReturn("hash-seguro");
        when(clienteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente creado = authService.registrar(registroValido());

        assertThat(creado.getPasswordHash()).isEqualTo("hash-seguro");
        assertThat(creado.getRol()).isEqualTo(RolUsuario.CLIENTE);
        assertThat(creado.getEstado()).isEqualTo(EstadoCliente.ACTIVO);
    }

    @Test
    void loginFallaSiElCorreoNoExiste() {
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Segura123")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFallaSiElClienteEstaInactivo() {
        Cliente cliente = clienteInactivo();
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Segura123")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFallaSiLaContraseniaNoCoincide() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("incorrecta", cliente.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "incorrecta")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginExitosoDevuelveTokenGenerado() {
        Cliente cliente = clienteActivo();
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("Segura123", cliente.getPasswordHash())).thenReturn(true);
        Instant expiracion = Instant.now().plusSeconds(3600);
        when(jwtService.generarToken(cliente.getId(), cliente.getRol()))
            .thenReturn(new JwtService.GeneratedToken("token-jwt", expiracion));

        LoginResponse response = authService.login(new LoginRequest("ana@example.com", "Segura123"));

        assertThat(response.token()).isEqualTo("token-jwt");
        assertThat(response.expiresAt()).isEqualTo(expiracion);
        org.mockito.Mockito.verify(auditLogService).registrar(cliente.getId(), "LOGIN_EXITOSO", "Cliente", cliente.getId(), null);
    }

    @Test
    void loginFallidoQuedaRegistradoEnAuditoria() {
        when(clienteRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "Segura123")))
            .isInstanceOf(InvalidCredentialsException.class);

        org.mockito.Mockito.verify(auditLogService)
            .registrarEnNuevaTransaccion(org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("LOGIN_FALLIDO"), org.mockito.ArgumentMatchers.eq("Cliente"),
                org.mockito.ArgumentMatchers.isNull(), anyString());
    }

    private Cliente clienteActivo() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setEmail("ana@example.com");
        cliente.setPasswordHash("hash-seguro");
        cliente.setEstado(EstadoCliente.ACTIVO);
        cliente.setRol(RolUsuario.CLIENTE);
        return cliente;
    }

    private Cliente clienteInactivo() {
        Cliente cliente = clienteActivo();
        cliente.setEstado(EstadoCliente.INACTIVO);
        return cliente;
    }
}
