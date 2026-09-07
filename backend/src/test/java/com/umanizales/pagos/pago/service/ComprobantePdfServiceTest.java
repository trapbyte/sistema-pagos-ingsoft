package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.pago.dto.ComprobanteResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComprobantePdfServiceTest {

    private final ComprobantePdfService service = new ComprobantePdfService();

    @Test
    void generaUnPdfValidoConLosDatosDelComprobante() {
        ComprobanteResponse comprobante = new ComprobanteResponse(
            UUID.randomUUID(),
            "CMP-12345678",
            "Ana Gomez",
            "******6655",
            "CHEC",
            "CTR-000123",
            new BigDecimal("138500.00"),
            Instant.now()
        );

        byte[] pdf = service.generar(comprobante);

        assertThat(pdf).isNotEmpty();
        String cabecera = new String(pdf, 0, 4, StandardCharsets.US_ASCII);
        assertThat(cabecera).isEqualTo("%PDF");
    }
}
