package com.umanizales.pagos.factura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InscribirServicioRequest(
    @NotNull UUID empresaId,
    @NotBlank String numeroReferencia,
    String alias
) {
}
