package com.umanizales.pagos.factura.controller;

import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.factura.dto.FacturaResponse;
import com.umanizales.pagos.factura.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/servicios/{servicioId}/factura")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public ResponseEntity<FacturaResponse> obtenerFacturaVigente(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID servicioId
    ) {
        var factura = facturaService.obtenerFacturaVigente(user.clienteId(), servicioId);
        return ResponseEntity.ok(FacturaResponse.from(factura));
    }
}
