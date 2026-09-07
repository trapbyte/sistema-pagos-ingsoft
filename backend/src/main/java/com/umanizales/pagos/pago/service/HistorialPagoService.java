package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.cliente.entity.RolUsuario;
import com.umanizales.pagos.common.exception.BusinessRuleException;
import com.umanizales.pagos.common.security.AuthenticatedUser;
import com.umanizales.pagos.pago.dto.FiltroHistorialPago;
import com.umanizales.pagos.pago.entity.Pago;
import com.umanizales.pagos.pago.repository.PagoRepository;
import com.umanizales.pagos.pago.repository.PagoSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CU-33/34: historial consolidado con filtros combinables. CU-35: exportación a CSV.
 */
@Service
public class HistorialPagoService {

    private static final int LIMITE_EXPORTACION = 5000;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_INSTANT;

    private final PagoRepository pagoRepository;

    public HistorialPagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pago> buscar(AuthenticatedUser user, FiltroHistorialPago filtro) {
        Specification<Pago> spec = construirEspecificacion(user, filtro);
        return pagoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaHora"));
    }

    @Transactional(readOnly = true)
    public String exportarCsv(AuthenticatedUser user, FiltroHistorialPago filtro) {
        List<Pago> pagos = buscar(user, filtro);

        // CU-35, FA01: si excede el límite, se pide acotar el filtro en vez de generar
        // un archivo gigante (o programar un envío por correo, fuera de alcance sin SMTP).
        if (pagos.size() > LIMITE_EXPORTACION) {
            throw new BusinessRuleException(
                "El resultado supera los " + LIMITE_EXPORTACION + " registros; acota los filtros e intenta de nuevo");
        }

        StringBuilder csv = new StringBuilder("codigoComprobante,fechaHora,monto,estado,tipoProcesamiento\n");
        for (Pago pago : pagos) {
            csv.append(pago.getCodigoComprobante()).append(',')
                .append(FORMATO_FECHA.format(pago.getFechaHora())).append(',')
                .append(pago.getMonto()).append(',')
                .append(pago.getEstado()).append(',')
                .append(pago.getTipoProcesamiento()).append('\n');
        }
        return csv.toString();
    }

    private Specification<Pago> construirEspecificacion(AuthenticatedUser user, FiltroHistorialPago filtro) {
        List<Specification<Pago>> predicados = new ArrayList<>();

        // Evita IDOR: un cliente nunca ve pagos de otro, sin importar qué filtre.
        if (user.rol() != RolUsuario.ADMINISTRADOR) {
            predicados.add(PagoSpecifications.deCliente(user.clienteId()));
        }
        if (filtro.cuentaId() != null) {
            predicados.add(PagoSpecifications.deCuenta(filtro.cuentaId()));
        }
        if (filtro.tipoCuenta() != null) {
            predicados.add(PagoSpecifications.deTipoCuenta(filtro.tipoCuenta()));
        }
        if (filtro.empresaId() != null) {
            predicados.add(PagoSpecifications.deEmpresa(filtro.empresaId()));
        }
        if (filtro.estado() != null) {
            predicados.add(PagoSpecifications.conEstado(filtro.estado()));
        }
        if (filtro.fechaDesde() != null) {
            predicados.add(PagoSpecifications.desde(filtro.fechaDesde()));
        }
        if (filtro.fechaHasta() != null) {
            predicados.add(PagoSpecifications.hasta(filtro.fechaHasta()));
        }
        if (filtro.montoMinimo() != null) {
            predicados.add(PagoSpecifications.montoMinimo(filtro.montoMinimo()));
        }
        if (filtro.montoMaximo() != null) {
            predicados.add(PagoSpecifications.montoMaximo(filtro.montoMaximo()));
        }

        return predicados.stream().reduce(Specification.where(null), Specification::and);
    }
}
