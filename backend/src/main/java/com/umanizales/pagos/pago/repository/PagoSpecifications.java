package com.umanizales.pagos.pago.repository;

import com.umanizales.pagos.cliente.entity.TipoCuenta;
import com.umanizales.pagos.pago.entity.EstadoPago;
import com.umanizales.pagos.pago.entity.Pago;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * CU-34: predicados combinables para el historial de pagos. Cada filtro es opcional
 * (se omite cuando el valor es null) — se combinan con {@code Specification.and(...)}
 * en {@link com.umanizales.pagos.pago.service.HistorialPagoService}.
 */
public final class PagoSpecifications {

    private PagoSpecifications() {
    }

    public static Specification<Pago> deCliente(UUID clienteId) {
        return (root, query, cb) -> cb.equal(root.get("cuenta").get("cliente").get("id"), clienteId);
    }

    public static Specification<Pago> deCuenta(UUID cuentaId) {
        return (root, query, cb) -> cb.equal(root.get("cuenta").get("id"), cuentaId);
    }

    public static Specification<Pago> deTipoCuenta(TipoCuenta tipoCuenta) {
        return (root, query, cb) -> cb.equal(root.get("cuenta").get("tipo"), tipoCuenta);
    }

    public static Specification<Pago> deEmpresa(UUID empresaId) {
        return (root, query, cb) -> cb.equal(root.get("factura").get("empresa").get("id"), empresaId);
    }

    public static Specification<Pago> conEstado(EstadoPago estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Pago> desde(LocalDate fecha) {
        Instant inicio = fecha.atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaHora"), inicio);
    }

    public static Specification<Pago> hasta(LocalDate fecha) {
        Instant finDelDia = fecha.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, query, cb) -> cb.lessThan(root.get("fechaHora"), finDelDia);
    }

    public static Specification<Pago> montoMinimo(BigDecimal monto) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("monto"), monto);
    }

    public static Specification<Pago> montoMaximo(BigDecimal monto) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("monto"), monto);
    }
}
