package com.umanizales.pagos.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActualizarPerfilRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    String telefono,
    @NotBlank @Email String email
) {
}
