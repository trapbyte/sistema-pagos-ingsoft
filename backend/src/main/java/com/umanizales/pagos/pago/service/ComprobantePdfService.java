package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.pago.dto.ComprobanteResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Genera el comprobante de pago en PDF (CU-28) a partir de la misma estructura de
 * datos que ya expone CU-27 ({@link ComprobanteResponse}).
 */
@Service
public class ComprobantePdfService {

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneOffset.UTC);

    public byte[] generar(ComprobanteResponse comprobante) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            List<String> lineas = List.of(
                "Comprobante de Pago",
                "",
                "Código de transacción: " + comprobante.codigoComprobante(),
                "Cliente: " + comprobante.clienteNombreCompleto(),
                "Cuenta origen: " + comprobante.cuentaEnmascarada(),
                "Servicio: " + comprobante.empresaServicio(),
                "Referencia: " + comprobante.numeroReferencia(),
                "Monto pagado: $" + comprobante.monto(),
                "Fecha y hora: " + FORMATO_FECHA.format(comprobante.fechaHora())
            );

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.beginText();
                content.newLineAtOffset(60, 750);
                for (String linea : lineas) {
                    content.showText(linea);
                    content.newLineAtOffset(0, -22);
                }
                content.endText();
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF del comprobante", e);
        }
    }
}
