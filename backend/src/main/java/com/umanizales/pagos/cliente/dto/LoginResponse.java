package com.umanizales.pagos.cliente.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
