package com.umanizales.pagos.factura.controller;

import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.factura.dto.ActualizarAliasServicioRequest;
import com.umanizales.pagos.factura.dto.InscribirServicioRequest;
import com.umanizales.pagos.factura.dto.ServicioInscritoResponse;
import com.umanizales.pagos.factura.service.ServicioInscritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/servicios")
public class ServicioInscritoController {

    private final ServicioInscritoService servicioInscritoService;

    public ServicioInscritoController(ServicioInscritoService servicioInscritoService) {
        this.servicioInscritoService = servicioInscritoService;
    }

    @PostMapping
    public ResponseEntity<ServicioInscritoResponse> inscribir(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody InscribirServicioRequest request
    ) {
        var servicio = servicioInscritoService.inscribir(user.clienteId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServicioInscritoResponse.from(servicio));
    }

    @GetMapping
    public ResponseEntity<List<ServicioInscritoResponse>> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        List<ServicioInscritoResponse> servicios = servicioInscritoService.listar(user.clienteId()).stream()
            .map(ServicioInscritoResponse::from)
            .toList();
        return ResponseEntity.ok(servicios);
    }

    @PatchMapping("/{servicioId}")
    public ResponseEntity<ServicioInscritoResponse> actualizarAlias(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID servicioId,
        @RequestBody ActualizarAliasServicioRequest request
    ) {
        var servicio = servicioInscritoService.actualizarAlias(user.clienteId(), servicioId, request);
        return ResponseEntity.ok(ServicioInscritoResponse.from(servicio));
    }

    @DeleteMapping("/{servicioId}")
    public ResponseEntity<Void> desinscribir(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID servicioId
    ) {
        servicioInscritoService.desinscribir(user.clienteId(), servicioId);
        return ResponseEntity.noContent().build();
    }
}
