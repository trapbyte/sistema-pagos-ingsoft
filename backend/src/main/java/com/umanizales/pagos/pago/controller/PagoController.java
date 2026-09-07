package com.umanizales.pagos.pago.controller;

import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.pago.dto.ComprobanteResponse;
import com.umanizales.pagos.pago.dto.PagoResponse;
import com.umanizales.pagos.pago.dto.RegistrarPagoLoteRequest;
import com.umanizales.pagos.pago.dto.RegistrarPagoRequest;
import com.umanizales.pagos.pago.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> registrarPago(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody RegistrarPagoRequest request
    ) {
        var pago = pagoService.registrarPago(user.clienteId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PagoResponse.from(pago));
    }

    @PostMapping("/lote")
    public ResponseEntity<List<PagoResponse>> registrarPagoLote(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody RegistrarPagoLoteRequest request
    ) {
        var pagos = pagoService.registrarPagoLote(user.clienteId(), request).stream()
            .map(PagoResponse::from)
            .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(pagos);
    }

    @GetMapping("/{pagoId}")
    public ResponseEntity<PagoResponse> obtener(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID pagoId
    ) {
        var pago = pagoService.obtenerDelCliente(user.clienteId(), pagoId);
        return ResponseEntity.ok(PagoResponse.from(pago));
    }

    @GetMapping("/{pagoId}/comprobante")
    public ResponseEntity<ComprobanteResponse> comprobante(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID pagoId
    ) {
        return ResponseEntity.ok(pagoService.generarComprobante(user.clienteId(), pagoId));
    }
}
