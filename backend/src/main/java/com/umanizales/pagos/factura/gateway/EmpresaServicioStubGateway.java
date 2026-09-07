package com.umanizales.pagos.factura.gateway;

import com.umanizales.pagos.factura.entity.EmpresaServicio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Implementación provisional: genera una factura simulada con un monto pseudoaleatorio
 * y vencimiento a 15 días, ya que aún no hay integración real con las empresas de servicio.
 */
@Component
public class EmpresaServicioStubGateway implements EmpresaServicioGateway {

    private static final BigDecimal MONTO_BASE = new BigDecimal("50000");
    private static final BigDecimal MONTO_VARIABLE_MAXIMO = new BigDecimal("150000");

    @Override
    public FacturaExterna consultarFacturaVigente(EmpresaServicio empresa, String numeroReferencia) {
        BigDecimal variacion = MONTO_VARIABLE_MAXIMO
            .multiply(BigDecimal.valueOf(Math.abs(numeroReferencia.hashCode()) % 100))
            .divide(BigDecimal.valueOf(100));
        BigDecimal monto = MONTO_BASE.add(variacion).setScale(2, java.math.RoundingMode.HALF_UP);

        LocalDate hoy = LocalDate.now();
        return new FacturaExterna(monto, hoy, hoy.plusDays(15));
    }
}
