package com.umanizales.pagos.cliente.dto;

import com.umanizales.pagos.common.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroClienteRequest(
    @NotBlank String documentoIdentidad,
    @NotBlank String tipoDocumento,
    @NotBlank String nombre,
    @NotBlank String apellido,
    @NotBlank @Email String email,
    String telefono,
    @StrongPassword String password
) {
}
