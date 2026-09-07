package com.umanizales.pagos.pago.service;

import com.umanizales.pagos.pago.dto.ComprobanteResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Genera el comprobante de pago en PDF (CU-28) a partir de la misma estructura de datos
 * que ya expone CU-27 ({@link ComprobanteResponse}). Los colores replican la paleta de
 * marca del frontend (ver frontend/src/styles/tokens.css) para que el PDF se sienta
 * parte del mismo producto en vez de un volcado de texto plano.
 */
@Service
public class ComprobantePdfService {

    private static final DateTimeFormatter FORMATO_FECHA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a").withZone(ZoneOffset.UTC);

    private static final Color INK = new Color(0x12, 0x18, 0x1C);
    private static final Color INK_SOFT = new Color(0x41, 0x4A, 0x4F);
    private static final Color PAPER = new Color(0xF1, 0xF3, 0xEF);
    private static final Color TEAL = new Color(0x0E, 0x6E, 0x5D);
    private static final Color COPPER = new Color(0xC7, 0x7B, 0x3C);
    private static final Color LINE = new Color(0xDA, 0xD9, 0xD0);
    private static final Color WHITE_SOFT = new Color(0xD8, 0xDE, 0xDC);

    private static final float MARGIN = 50f;
    private static final float HEADER_HEIGHT = 108f;

    private final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    public byte[] generar(ComprobanteResponse comprobante) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float contentWidth = pageWidth - 2 * MARGIN;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                dibujarEncabezado(content, pageWidth, pageHeight);
                float y = dibujarResumen(content, comprobante, pageWidth, pageHeight, contentWidth);
                y = dibujarGrid(content, comprobante, y, contentWidth);
                dibujarPie(content, pageWidth, y);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF del comprobante", e);
        }
    }

    private void dibujarEncabezado(PDPageContentStream content, float pageWidth, float pageHeight) throws IOException {
        rect(content, 0, pageHeight - HEADER_HEIGHT, pageWidth, HEADER_HEIGHT, INK);

        // Logomark: mismo motivo del favicon (marco claro con líneas), sin depender de una imagen.
        float logoX = MARGIN;
        float logoY = pageHeight - 76;
        rect(content, logoX, logoY, 34, 34, PAPER);
        for (int i = 0; i < 3; i++) {
            line(content, logoX + 7, logoY + 23 - (i * 7), logoX + 27, logoY + 23 - (i * 7), COPPER, 1.6f);
        }

        text(content, bold, 20, Color.WHITE, logoX + 46, pageHeight - 52, "Recibo");
        text(content, regular, 10.5f, WHITE_SOFT, logoX + 46, pageHeight - 70, "Comprobante de pago");
    }

    private float dibujarResumen(PDPageContentStream content, ComprobanteResponse comprobante, float pageWidth,
                                  float pageHeight, float contentWidth) throws IOException {
        float y = pageHeight - HEADER_HEIGHT - 42;

        text(content, regular, 9.5f, INK_SOFT, MARGIN, y, "Código de comprobante");
        text(content, bold, 17, INK, MARGIN, y - 20, comprobante.codigoComprobante());

        String monto = formatearMonto(comprobante.monto());
        textDerecha(content, regular, 9.5f, INK_SOFT, MARGIN + contentWidth, y, "Monto pagado");
        textDerecha(content, bold, 22, TEAL, MARGIN + contentWidth, y - 24, monto);

        float lineaY = y - 40;
        line(content, MARGIN, lineaY, MARGIN + contentWidth, lineaY, LINE, 1f);
        return lineaY;
    }

    private float dibujarGrid(PDPageContentStream content, ComprobanteResponse comprobante, float topY,
                               float contentWidth) throws IOException {
        record Campo(String etiqueta, String valor) {
        }

        List<Campo> campos = List.of(
            new Campo("Cliente", comprobante.clienteNombreCompleto()),
            new Campo("Cuenta origen", comprobante.cuentaEnmascarada()),
            new Campo("Servicio", comprobante.empresaServicio()),
            new Campo("Referencia", comprobante.numeroReferencia()),
            new Campo("Fecha y hora", FORMATO_FECHA.format(comprobante.fechaHora()) + " UTC")
        );

        float colGap = 24f;
        float colWidth = (contentWidth - colGap) / 2;
        float col1X = MARGIN;
        float col2X = MARGIN + colWidth + colGap;
        float rowHeight = 52f;
        float y = topY - 34;

        for (int i = 0; i < campos.size(); i += 2) {
            Campo izquierda = campos.get(i);
            text(content, regular, 9, INK_SOFT, col1X, y, izquierda.etiqueta());
            text(content, bold, 11.5f, INK, col1X, y - 17, izquierda.valor());

            if (i + 1 < campos.size()) {
                Campo derecha = campos.get(i + 1);
                text(content, regular, 9, INK_SOFT, col2X, y, derecha.etiqueta());
                text(content, bold, 11.5f, INK, col2X, y - 17, derecha.valor());
            }

            if (i + 2 < campos.size()) {
                line(content, MARGIN, y - 34, MARGIN + contentWidth, y - 34, LINE, 0.75f);
            }
            y -= rowHeight;
        }

        return y;
    }

    private void dibujarPie(PDPageContentStream content, float pageWidth, float y) throws IOException {
        float lineaY = Math.min(y, 130f);
        line(content, MARGIN, lineaY, pageWidth - MARGIN, lineaY, LINE, 1f);
        text(content, regular, 8.5f, INK_SOFT, MARGIN, lineaY - 18,
            "Este comprobante es válido como constancia de pago.");
        String generado = "Generado el " + FORMATO_FECHA.format(Instant.now()) + " UTC";
        text(content, regular, 8.5f, INK_SOFT, MARGIN, lineaY - 32, generado);
        textDerecha(content, bold, 8.5f, INK_SOFT, pageWidth - MARGIN, lineaY - 32,
            "Recibo · Sistema de Pagos de Servicios Públicos");
    }

    private String formatearMonto(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getNumberInstance(new Locale("es", "CO"));
        formato.setMaximumFractionDigits(0);
        return "$ " + formato.format(monto);
    }

    private void rect(PDPageContentStream content, float x, float y, float w, float h, Color color) throws IOException {
        content.setNonStrokingColor(color);
        content.addRect(x, y, w, h);
        content.fill();
    }

    private void line(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color, float width)
        throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(width);
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private void text(PDPageContentStream content, PDFont font, float size, Color color, float x, float y, String value)
        throws IOException {
        content.setNonStrokingColor(color);
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(value);
        content.endText();
    }

    private void textDerecha(PDPageContentStream content, PDFont font, float size, Color color, float rightX, float y,
                              String value) throws IOException {
        float width = font.getStringWidth(value) / 1000 * size;
        text(content, font, size, color, rightX - width, y, value);
    }
}
