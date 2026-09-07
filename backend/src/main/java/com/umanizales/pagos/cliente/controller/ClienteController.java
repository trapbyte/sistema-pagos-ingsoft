package com.umanizales.pagos.cliente.controller;

import com.umanizales.pagos.cliente.dto.ActualizarPerfilRequest;
import com.umanizales.pagos.cliente.dto.ClientePerfilResponse;
import com.umanizales.pagos.cliente.service.ClienteService;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes/me")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<ClientePerfilResponse> obtenerPerfil(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ClientePerfilResponse.from(clienteService.obtener(user.clienteId())));
    }

    @PatchMapping
    public ResponseEntity<ClientePerfilResponse> actualizarPerfil(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody ActualizarPerfilRequest request
    ) {
        var cliente = clienteService.actualizarPerfil(user.clienteId(), request);
        return ResponseEntity.ok(ClientePerfilResponse.from(cliente));
    }

    @DeleteMapping
    public ResponseEntity<Void> inactivar(@AuthenticationPrincipal AuthenticatedUser user) {
        clienteService.inactivar(user.clienteId());
        return ResponseEntity.noContent().build();
    }
}
