package com.umanizales.pagos.reversion.controller;

import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.reversion.dto.DecidirReversionRequest;
import com.umanizales.pagos.reversion.dto.ReversionResponse;
import com.umanizales.pagos.reversion.dto.SolicitarReversionRequest;
import com.umanizales.pagos.reversion.service.ReversionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reversiones")
public class ReversionController {

    private final ReversionService reversionService;

    public ReversionController(ReversionService reversionService) {
        this.reversionService = reversionService;
    }

    @PostMapping
    public ResponseEntity<ReversionResponse> solicitar(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody SolicitarReversionRequest request
    ) {
        var reversion = reversionService.solicitar(user.clienteId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReversionResponse.from(reversion));
    }

    @GetMapping
    public ResponseEntity<List<ReversionResponse>> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        var reversiones = reversionService.listar(user).stream()
            .map(ReversionResponse::from)
            .toList();
        return ResponseEntity.ok(reversiones);
    }

    @GetMapping("/{reversionId}")
    public ResponseEntity<ReversionResponse> obtener(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID reversionId
    ) {
        var reversion = reversionService.obtener(user, reversionId);
        return ResponseEntity.ok(ReversionResponse.from(reversion));
    }

    @PatchMapping("/{reversionId}/decision")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReversionResponse> decidir(
        @AuthenticationPrincipal AuthenticatedUser admin,
        @PathVariable UUID reversionId,
        @Valid @RequestBody DecidirReversionRequest request
    ) {
        var reversion = reversionService.decidir(admin.clienteId(), reversionId, request);
        return ResponseEntity.ok(ReversionResponse.from(reversion));
    }
}
