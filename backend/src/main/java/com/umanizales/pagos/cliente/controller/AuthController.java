package com.umanizales.pagos.cliente.controller;

import com.umanizales.pagos.cliente.dto.ClientePerfilResponse;
import com.umanizales.pagos.cliente.dto.LoginRequest;
import com.umanizales.pagos.cliente.dto.LoginResponse;
import com.umanizales.pagos.cliente.dto.RegistroClienteRequest;
import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ClientePerfilResponse> register(@Valid @RequestBody RegistroClienteRequest request) {
        Cliente cliente = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientePerfilResponse.from(cliente));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
