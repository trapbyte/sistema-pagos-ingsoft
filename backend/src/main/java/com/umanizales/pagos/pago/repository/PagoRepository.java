package com.umanizales.pagos.pago.repository;

import com.umanizales.pagos.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {

    List<Pago> findByCuentaIdOrderByFechaHoraDesc(UUID cuentaId);
}
