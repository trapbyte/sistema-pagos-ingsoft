package com.umanizales.pagos.cliente.dto;

import com.umanizales.pagos.cliente.entity.Cliente;
import com.umanizales.pagos.cliente.entity.EstadoCliente;

import java.util.UUID;

public record ClientePerfilResponse(
    UUID id,
    String documentoIdentidad,
    String tipoDocumento,
    String nombre,
    String apellido,
    String email,
    String telefono,
    EstadoCliente estado
) {
    public static ClientePerfilResponse from(Cliente cliente) {
        return new ClientePerfilResponse(
            cliente.getId(),
            cliente.getDocumentoIdentidad(),
            cliente.getTipoDocumento(),
            cliente.getNombre(),
            cliente.getApellido(),
            cliente.getEmail(),
            cliente.getTelefono(),
            cliente.getEstado()
        );
    }
}
