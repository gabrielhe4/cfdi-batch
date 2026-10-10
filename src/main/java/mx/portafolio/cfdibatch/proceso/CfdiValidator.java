package mx.portafolio.cfdibatch.proceso;

import mx.portafolio.cfdibatch.xml.Comprobante;
import mx.portafolio.cfdibatch.xml.Concepto;
import mx.portafolio.cfdibatch.xml.Impuesto;
import mx.portafolio.cfdibatch.xml.Impuestos;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de negocio sobre un CFDI ya leído. Devuelve TODOS los errores encontrados,
 * no solo el primero, para que el reporte de rechazos sea útil.
 */
@Component
public class CfdiValidator {

    /** Persona moral (12) o física (13). */
    private static final Pattern RFC = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern UUID = Pattern.compile(
            "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");
    /** Tolerancia por redondeo. */
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");

    public List<String> validar(Comprobante c) {
        List<String> errores = new ArrayList<>();

        if (!"4.0".equals(c.getVersion())) {
            errores.add("Versión no soportada: " + c.getVersion());
        }
        if (fechaInvalida(c.getFecha())) {
            errores.add("Fecha inválida: " + c.getFecha());
        }
        validarRfc("emisor", c.getEmisor() == null ? null : c.getEmisor().getRfc(), errores);
        validarRfc("receptor", c.getReceptor() == null ? null : c.getReceptor().getRfc(), errores);
        validarTimbre(c, errores);
        validarImportes(c, errores);

        return errores;
    }

    private void validarRfc(String quien, String rfc, List<String> errores) {
        if (rfc == null || !RFC.matcher(rfc).matches()) {
            errores.add("RFC del " + quien + " inválido: " + rfc);
        }
    }

    private void validarTimbre(Comprobante c, List<String> errores) {
        if (c.getTimbre() == null) {
            errores.add("CFDI sin TimbreFiscalDigital (no timbrado)");
            return;
        }
        if (c.getUuid() == null || !UUID.matcher(c.getUuid()).matches()) {
            errores.add("UUID inválido: " + c.getUuid());
        }
        if (fechaInvalida(c.getTimbre().getFechaTimbrado())) {
            errores.add("FechaTimbrado inválida: " + c.getTimbre().getFechaTimbrado());
        }
    }

    private void validarImportes(Comprobante c, List<String> errores) {
        if (c.getSubTotal() == null || c.getTotal() == null) {
            errores.add("SubTotal y Total son obligatorios");
            return;
        }
        if (c.getConceptos().isEmpty()) {
            errores.add("El CFDI no tiene conceptos");
        } else {
            BigDecimal sumaConceptos = c.getConceptos().stream()
                    .map(Concepto::getImporte)
                    .map(CfdiValidator::nvl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (difiere(sumaConceptos, c.getSubTotal())) {
                errores.add("La suma de importes de conceptos (" + sumaConceptos
                        + ") no coincide con SubTotal (" + c.getSubTotal() + ")");
            }
        }

        Impuestos imp = c.getImpuestos();
        BigDecimal trasladados = imp == null ? BigDecimal.ZERO : nvl(imp.getTotalImpuestosTrasladados());
        BigDecimal retenidos = imp == null ? BigDecimal.ZERO : nvl(imp.getTotalImpuestosRetenidos());

        if (imp != null) {
            BigDecimal sumaTraslados = suma(imp.getTraslados());
            if (difiere(sumaTraslados, trasladados)) {
                errores.add("TotalImpuestosTrasladados (" + trasladados
                        + ") no coincide con la suma de traslados (" + sumaTraslados + ")");
            }
            BigDecimal sumaRetenciones = suma(imp.getRetenciones());
            if (difiere(sumaRetenciones, retenidos)) {
                errores.add("TotalImpuestosRetenidos (" + retenidos
                        + ") no coincide con la suma de retenciones (" + sumaRetenciones + ")");
            }
        }

        BigDecimal esperado = c.getSubTotal()
                .subtract(nvl(c.getDescuento()))
                .add(trasladados)
                .subtract(retenidos);
        if (difiere(esperado, c.getTotal())) {
            errores.add("Total (" + c.getTotal() + ") no coincide con SubTotal - Descuento"
                    + " + Trasladados - Retenidos (" + esperado + ")");
        }
    }

    private static boolean fechaInvalida(String fecha) {
        if (fecha == null) {
            return true;
        }
        try {
            LocalDateTime.parse(fecha);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    private static BigDecimal suma(List<Impuesto> impuestos) {
        return impuestos.stream()
                .map(Impuesto::getImporte)
                .map(CfdiValidator::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean difiere(BigDecimal a, BigDecimal b) {
        return a.subtract(b).abs().compareTo(TOLERANCIA) > 0;
    }

    static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
