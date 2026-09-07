package com.umanizales.pagos.cliente.repository;

import com.umanizales.pagos.cliente.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {

    List<Cuenta> findByClienteId(UUID clienteId);

    Optional<Cuenta> findByIdAndClienteId(UUID id, UUID clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByClienteId(UUID clienteId);

    long countByClienteIdAndPredeterminadaTrue(UUID clienteId);
}
