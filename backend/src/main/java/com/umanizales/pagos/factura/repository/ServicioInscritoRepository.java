package com.umanizales.pagos.factura.repository;

import com.umanizales.pagos.factura.entity.ServicioInscrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicioInscritoRepository extends JpaRepository<ServicioInscrito, UUID> {

    // JOIN FETCH evita LazyInitializationException al mapear ServicioInscritoResponse
    // fuera de la transacción (open-in-view está deshabilitado a propósito).
    @Query("SELECT s FROM ServicioInscrito s JOIN FETCH s.empresa WHERE s.cliente.id = :clienteId")
    List<ServicioInscrito> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT s FROM ServicioInscrito s JOIN FETCH s.empresa WHERE s.id = :id AND s.cliente.id = :clienteId")
    Optional<ServicioInscrito> findByIdAndClienteId(@Param("id") UUID id, @Param("clienteId") UUID clienteId);

    boolean existsByClienteIdAndNumeroReferencia(UUID clienteId, String numeroReferencia);
}
