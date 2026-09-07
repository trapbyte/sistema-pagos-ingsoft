package com.umanizales.pagos.cliente.controller;

import com.umanizales.pagos.cliente.dto.ActualizarPreferenciaCuentaRequest;
import com.umanizales.pagos.cliente.dto.CuentaResponse;
import com.umanizales.pagos.cliente.dto.VincularCuentaRequest;
import com.umanizales.pagos.cliente.entity.Cuenta;
import com.umanizales.pagos.cliente.service.CuentaService;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.pago.dto.PagoResumenResponse;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> vincular(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody VincularCuentaRequest request
    ) {
        Cuenta cuenta = cuentaService.vincular(user.clienteId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaResponse.from(cuenta));
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        List<CuentaResponse> cuentas = cuentaService.listar(user.clienteId()).stream()
            .map(CuentaResponse::from)
            .toList();
        return ResponseEntity.ok(cuentas);
    }

    @PatchMapping("/{cuentaId}/preferencias")
    public ResponseEntity<CuentaResponse> actualizarPreferencias(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID cuentaId,
        @RequestBody ActualizarPreferenciaCuentaRequest request
    ) {
        Cuenta cuenta = cuentaService.actualizarPreferencias(user.clienteId(), cuentaId, request);
        return ResponseEntity.ok(CuentaResponse.from(cuenta));
    }

    @DeleteMapping("/{cuentaId}")
    public ResponseEntity<Void> desvincular(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID cuentaId
    ) {
        cuentaService.desvincular(user.clienteId(), cuentaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cuentaId}/saldo")
    public ResponseEntity<BigDecimal> saldo(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID cuentaId
    ) {
        return ResponseEntity.ok(cuentaService.obtenerDelCliente(user.clienteId(), cuentaId).getSaldo());
    }

    @GetMapping("/{cuentaId}/movimientos")
    public ResponseEntity<List<PagoResumenResponse>> movimientos(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID cuentaId
    ) {
        List<PagoResumenResponse> movimientos = cuentaService.movimientos(user.clienteId(), cuentaId).stream()
            .map(PagoResumenResponse::from)
            .toList();
        return ResponseEntity.ok(movimientos);
    }
}
