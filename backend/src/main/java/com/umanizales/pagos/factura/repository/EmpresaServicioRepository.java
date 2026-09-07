package com.umanizales.pagos.factura.repository;

import com.umanizales.pagos.factura.entity.EmpresaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmpresaServicioRepository extends JpaRepository<EmpresaServicio, UUID> {
}
