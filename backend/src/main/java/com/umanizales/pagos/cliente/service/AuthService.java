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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        AuditLogService auditLogService) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Cliente registrar(RegistroClienteRequest request) {
        // HU-01, CU-01 FA01: documento y correo únicos en el sistema.
        if (clienteRepository.existsByDocumentoIdentidad(request.documentoIdentidad())) {
            throw new DuplicateResourceException("El número de documento ya está registrado");
        }
        if (clienteRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El correo electrónico ya está registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setDocumentoIdentidad(request.documentoIdentidad());
        cliente.setTipoDocumento(request.tipoDocumento());
        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setPasswordHash(passwordEncoder.encode(request.password()));
        cliente.setEstado(EstadoCliente.ACTIVO);
        // El rol ADMINISTRADOR no es auto-asignable desde el registro público.
        cliente.setRol(RolUsuario.CLIENTE);

        return clienteRepository.save(cliente);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Cliente cliente = clienteRepository.findByEmail(request.email()).orElse(null);

        if (cliente == null || cliente.getEstado() != EstadoCliente.ACTIVO
            || !passwordEncoder.matches(request.password(), cliente.getPasswordHash())) {
            // Transacción propia: si se guardara en la misma transacción de este método,
            // el rollback al lanzar la excepción de abajo borraría este mismo registro.
            auditLogService.registrarEnNuevaTransaccion(
                cliente != null ? cliente.getId() : null,
                "LOGIN_FALLIDO", "Cliente", cliente != null ? cliente.getId() : null,
                "Intento de login con el correo " + request.email());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        JwtService.GeneratedToken generatedToken = jwtService.generarToken(cliente.getId(), cliente.getRol());
        auditLogService.registrar(cliente.getId(), "LOGIN_EXITOSO", "Cliente", cliente.getId(), null);

        return new LoginResponse(generatedToken.token(), generatedToken.expiresAt());
    }
}
