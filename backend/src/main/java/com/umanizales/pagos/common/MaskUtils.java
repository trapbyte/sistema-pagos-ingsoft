package com.umanizales.pagos.common;

public final class MaskUtils {

    private MaskUtils() {
    }

    public static String enmascararNumero(String numero) {
        if (numero == null || numero.length() <= 4) {
            return numero;
        }
        String ultimos4 = numero.substring(numero.length() - 4);
        return "*".repeat(numero.length() - 4) + ultimos4;
    }
}
