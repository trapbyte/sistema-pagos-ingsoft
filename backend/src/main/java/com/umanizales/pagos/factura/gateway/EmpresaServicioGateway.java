package com.umanizales.pagos.factura.gateway;

import com.umanizales.pagos.factura.entity.EmpresaServicio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Puerto hacia las Empresas de Servicios externas (CHEC, Efigas, Aguas, etc.).
 * La implementación real (REST/SOAP contra cada proveedor) se añade en una iteración
 * posterior; por ahora {@link EmpresaServicioStubGateway} simula la respuesta.
 */
public interface EmpresaServicioGateway {

    /**
     * Simula la consulta de la factura vigente de un contrato ante la empresa prestadora
     * (CU-15). Se usa solo cuando el sistema aún no tiene una factura registrada para esa
     * referencia.
     */
    FacturaExterna consultarFacturaVigente(EmpresaServicio empresa, String numeroReferencia);

    record FacturaExterna(BigDecimal montoTotal, LocalDate fechaEmision, LocalDate fechaVencimiento) {
    }
}
