package com.umanizales.pagos.pago.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Estructura del comprobante de pago (CU-27). La descarga en PDF (CU-28) se agrega
 * en una iteración posterior; por ahora expone estos mismos datos como JSON.
 */
public record ComprobanteResponse(
    UUID pagoId,
    String codigoComprobante,
    String clienteNombreCompleto,
    String cuentaEnmascarada,
    String empresaServicio,
    String numeroReferencia,
    BigDecimal monto,
    Instant fechaHora
) {
}
