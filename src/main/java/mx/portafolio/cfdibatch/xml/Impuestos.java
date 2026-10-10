package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Nodo Impuestos. Se usa tanto a nivel comprobante (con totales)
 * como a nivel concepto (sin totales).
 */
public class Impuestos {

    @XmlAttribute(name = "TotalImpuestosTrasladados")
    private BigDecimal totalImpuestosTrasladados;
    @XmlAttribute(name = "TotalImpuestosRetenidos")
    private BigDecimal totalImpuestosRetenidos;

    @XmlElementWrapper(name = "Traslados")
    @XmlElement(name = "Traslado")
    private List<Impuesto> traslados = new ArrayList<>();

    @XmlElementWrapper(name = "Retenciones")
    @XmlElement(name = "Retencion")
    private List<Impuesto> retenciones = new ArrayList<>();

    public BigDecimal getTotalImpuestosTrasladados() { return totalImpuestosTrasladados; }
    public BigDecimal getTotalImpuestosRetenidos() { return totalImpuestosRetenidos; }
    public List<Impuesto> getTraslados() { return traslados == null ? List.of() : traslados; }
    public List<Impuesto> getRetenciones() { return retenciones == null ? List.of() : retenciones; }
}
