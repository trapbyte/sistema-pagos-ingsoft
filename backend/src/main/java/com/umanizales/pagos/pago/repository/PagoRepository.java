package com.umanizales.pagos.pago.repository;

import com.umanizales.pagos.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID>, JpaSpecificationExecutor<Pago> {

    List<Pago> findByCuentaIdOrderByFechaHoraDesc(UUID cuentaId);
}
