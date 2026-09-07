package com.umanizales.pagos.common.security;

import com.umanizales.pagos.cliente.entity.RolUsuario;

import java.util.UUID;

public record AuthenticatedUser(UUID clienteId, RolUsuario rol) {
}
