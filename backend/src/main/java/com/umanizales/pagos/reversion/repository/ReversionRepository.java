package com.umanizales.pagos.reversion.repository;

import com.umanizales.pagos.reversion.entity.Reversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReversionRepository extends JpaRepository<Reversion, UUID> {

    // JOIN FETCH evita LazyInitializationException al mapear el DTO fuera de la
    // transacción (mismo patrón usado en los demás repositorios del proyecto).
    @Query("SELECT r FROM Reversion r JOIN FETCH r.pago WHERE r.pago.cuenta.cliente.id = :clienteId")
    List<Reversion> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT r FROM Reversion r JOIN FETCH r.pago")
    List<Reversion> findAllConPago();

    @Query("SELECT r FROM Reversion r JOIN FETCH r.pago WHERE r.id = :id AND r.pago.cuenta.cliente.id = :clienteId")
    Optional<Reversion> findByIdAndClienteId(@Param("id") UUID id, @Param("clienteId") UUID clienteId);

    @Query("SELECT r FROM Reversion r JOIN FETCH r.pago WHERE r.id = :id")
    Optional<Reversion> findByIdConPago(@Param("id") UUID id);

    boolean existsByPagoId(UUID pagoId);
}
