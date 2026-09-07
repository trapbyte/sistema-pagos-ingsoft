package com.umanizales.pagos.pago.scheduler;

import com.umanizales.pagos.pago.service.DomiciliacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CU-26: ejecuta diariamente el débito automático de las domiciliaciones activas.
 * También se puede disparar bajo demanda vía {@code POST /api/domiciliaciones/ejecutar}
 * (ver {@link com.umanizales.pagos.pago.controller.DomiciliacionController}) para
 * verificarlo sin esperar al cron.
 */
@Component
public class DomiciliacionBatchJob {

    private static final Logger logger = LoggerFactory.getLogger(DomiciliacionBatchJob.class);

    private final DomiciliacionService domiciliacionService;

    public DomiciliacionBatchJob(DomiciliacionService domiciliacionService) {
        this.domiciliacionService = domiciliacionService;
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void ejecutarDomiciliacionesDelDia() {
        logger.info("Iniciando proceso batch de domiciliaciones del día");
        domiciliacionService.ejecutarDomiciliacionesDelDia();
        logger.info("Proceso batch de domiciliaciones finalizado");
    }
}
