package com.umanizales.pagos.pago.controller;

import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.pago.dto.ActivarDomiciliacionRequest;
import com.umanizales.pagos.pago.dto.ActualizarDomiciliacionRequest;
import com.umanizales.pagos.pago.dto.DomiciliacionResponse;
import com.umanizales.pagos.pago.service.DomiciliacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/domiciliaciones")
public class DomiciliacionController {

    private final DomiciliacionService domiciliacionService;

    public DomiciliacionController(DomiciliacionService domiciliacionService) {
        this.domiciliacionService = domiciliacionService;
    }

    @PostMapping
    public ResponseEntity<DomiciliacionResponse> activar(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody ActivarDomiciliacionRequest request
    ) {
        var domiciliacion = domiciliacionService.activar(user.clienteId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DomiciliacionResponse.from(domiciliacion));
    }

    @GetMapping
    public ResponseEntity<List<DomiciliacionResponse>> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        var domiciliaciones = domiciliacionService.listar(user.clienteId()).stream()
            .map(DomiciliacionResponse::from)
            .toList();
        return ResponseEntity.ok(domiciliaciones);
    }

    @PatchMapping("/{domiciliacionId}")
    public ResponseEntity<DomiciliacionResponse> actualizar(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID domiciliacionId,
        @RequestBody ActualizarDomiciliacionRequest request
    ) {
        var domiciliacion = domiciliacionService.actualizar(user.clienteId(), domiciliacionId, request);
        return ResponseEntity.ok(DomiciliacionResponse.from(domiciliacion));
    }

    /**
     * Dispara manualmente el proceso batch de CU-26 para poder verificarlo sin esperar
     * al cron diario. TODO: restringir a rol ADMINISTRADOR cuando exista ese mecanismo
     * (hoy cualquier cliente autenticado puede llamarlo, ver plan de Módulo C parte 2).
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<Void> ejecutarBatch() {
        domiciliacionService.ejecutarDomiciliacionesDelDia();
        return ResponseEntity.noContent().build();
    }
}
