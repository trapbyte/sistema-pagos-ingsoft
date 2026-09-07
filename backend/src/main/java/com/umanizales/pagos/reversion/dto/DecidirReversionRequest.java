package com.umanizales.pagos.reversion.dto;

import jakarta.validation.constraints.NotNull;

public record DecidirReversionRequest(
    @NotNull Boolean aprobar,
    String respuesta
) {
}
