package com.umanizales.pagos.factura.controller;

import com.umanizales.pagos.factura.dto.EmpresaServicioResponse;
import com.umanizales.pagos.factura.repository.EmpresaServicioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Listado de solo lectura de empresas de servicio disponibles para inscripción (CU-11).
 * El alta/gestión de empresas (rol Administrador) queda fuera de alcance de esta iteración.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaServicioController {

    private final EmpresaServicioRepository empresaServicioRepository;

    public EmpresaServicioController(EmpresaServicioRepository empresaServicioRepository) {
        this.empresaServicioRepository = empresaServicioRepository;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaServicioResponse>> listar() {
        List<EmpresaServicioResponse> empresas = empresaServicioRepository.findAll().stream()
            .map(EmpresaServicioResponse::from)
            .toList();
        return ResponseEntity.ok(empresas);
    }
}
