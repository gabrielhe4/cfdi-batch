package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;

import java.math.BigDecimal;

/** Un Traslado o una Retencion (comparten atributos). */
public class Impuesto {

    @XmlAttribute(name = "Base")
    private BigDecimal base;
    /** 001 = ISR, 002 = IVA, 003 = IEPS */
    @XmlAttribute(name = "Impuesto")
    private String impuesto;
    @XmlAttribute(name = "TipoFactor")
    private String tipoFactor;
    @XmlAttribute(name = "TasaOCuota")
    private BigDecimal tasaOCuota;
    @XmlAttribute(name = "Importe")
    private BigDecimal importe;

    public BigDecimal getBase() { return base; }
    public String getImpuesto() { return impuesto; }
    public String getTipoFactor() { return tipoFactor; }
    public BigDecimal getTasaOCuota() { return tasaOCuota; }
    public BigDecimal getImporte() { return importe; }
}
