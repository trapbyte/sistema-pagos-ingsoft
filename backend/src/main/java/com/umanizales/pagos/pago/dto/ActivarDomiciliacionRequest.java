package com.umanizales.pagos.pago.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActivarDomiciliacionRequest(
    @NotNull UUID servicioInscritoId,
    @NotNull UUID cuentaId
) {
}
