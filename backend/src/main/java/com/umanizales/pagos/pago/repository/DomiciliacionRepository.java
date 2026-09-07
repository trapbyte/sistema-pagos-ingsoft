package com.umanizales.pagos.pago.repository;

import com.umanizales.pagos.pago.entity.Domiciliacion;
import com.umanizales.pagos.pago.entity.EstadoDomiciliacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomiciliacionRepository extends JpaRepository<Domiciliacion, UUID> {

    // JOIN FETCH evita LazyInitializationException al mapear el DTO fuera de la
    // transacción (mismo patrón que ServicioInscritoRepository).
    @Query("SELECT d FROM Domiciliacion d JOIN FETCH d.servicioInscrito JOIN FETCH d.cuenta " +
        "WHERE d.servicioInscrito.cliente.id = :clienteId")
    List<Domiciliacion> findByServicioInscritoClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT d FROM Domiciliacion d JOIN FETCH d.servicioInscrito JOIN FETCH d.cuenta " +
        "WHERE d.id = :id AND d.servicioInscrito.cliente.id = :clienteId")
    Optional<Domiciliacion> findByIdAndServicioInscritoClienteId(@Param("id") UUID id, @Param("clienteId") UUID clienteId);

    Optional<Domiciliacion> findByServicioInscritoId(UUID servicioInscritoId);

    @Query("SELECT d FROM Domiciliacion d JOIN FETCH d.servicioInscrito si JOIN FETCH si.empresa JOIN FETCH d.cuenta " +
        "WHERE d.estado = :estado")
    List<Domiciliacion> findByEstado(@Param("estado") EstadoDomiciliacion estado);
}
