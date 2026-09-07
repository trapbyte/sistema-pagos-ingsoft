package com.umanizales.pagos.factura.repository;

import com.umanizales.pagos.factura.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FacturaRepository extends JpaRepository<Factura, UUID> {

    Optional<Factura> findByNumeroReferencia(String numeroReferencia);
}
