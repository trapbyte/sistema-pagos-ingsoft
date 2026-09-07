package com.umanizales.pagos.factura.dto;

import com.umanizales.pagos.factura.entity.EmpresaServicio;

import java.util.UUID;

public record EmpresaServicioResponse(UUID id, String razonSocial, String categoria) {

    public static EmpresaServicioResponse from(EmpresaServicio empresa) {
        return new EmpresaServicioResponse(empresa.getId(), empresa.getRazonSocial(), empresa.getCategoria());
    }
}
