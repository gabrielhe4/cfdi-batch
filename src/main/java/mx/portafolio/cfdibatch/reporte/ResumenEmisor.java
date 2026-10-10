package mx.portafolio.cfdibatch.reporte;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Una fila del reporte: totales por emisor y por mes. */
public record ResumenEmisor(
        String rfc,
        String nombre,
        int anio,
        int mes,
        long cantidad,
        BigDecimal subtotalNeto,
        BigDecimal trasladados,
        BigDecimal retenidos,
        BigDecimal total) {

    public static final String ENCABEZADO =
            "rfc_emisor,nombre_emisor,anio,mes,cfdis,subtotal_neto,impuestos_trasladados,impuestos_retenidos,total";

    public String aCsv() {
        return String.join(",",
                rfc,
                "\"" + (nombre == null ? "" : nombre.replace("\"", "\"\"")) + "\"",
                String.valueOf(anio),
                String.format("%02d", mes),
                String.valueOf(cantidad),
                dinero(subtotalNeto),
                dinero(trasladados),
                dinero(retenidos),
                dinero(total));
    }

    private static String dinero(BigDecimal valor) {
        return valor == null ? "0.00" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
