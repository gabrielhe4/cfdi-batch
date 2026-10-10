package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Comprobante")
public class Comprobante {

    @XmlAttribute(name = "Version")
    private String version;
    @XmlAttribute(name = "Serie")
    private String serie;
    @XmlAttribute(name = "Folio")
    private String folio;
    @XmlAttribute(name = "Fecha")
    private String fecha;
    @XmlAttribute(name = "FormaPago")
    private String formaPago;
    @XmlAttribute(name = "SubTotal")
    private BigDecimal subTotal;
    @XmlAttribute(name = "Descuento")
    private BigDecimal descuento;
    @XmlAttribute(name = "Moneda")
    private String moneda;
    @XmlAttribute(name = "TipoCambio")
    private BigDecimal tipoCambio;
    @XmlAttribute(name = "Total")
    private BigDecimal total;
    @XmlAttribute(name = "TipoDeComprobante")
    private String tipoDeComprobante;
    @XmlAttribute(name = "MetodoPago")
    private String metodoPago;
    @XmlAttribute(name = "LugarExpedicion")
    private String lugarExpedicion;

    @XmlElement(name = "Emisor")
    private Emisor emisor;
    @XmlElement(name = "Receptor")
    private Receptor receptor;

    @XmlElementWrapper(name = "Conceptos")
    @XmlElement(name = "Concepto")
    private List<Concepto> conceptos = new ArrayList<>();

    @XmlElement(name = "Impuestos")
    private Impuestos impuestos;

    @XmlElement(name = "Complemento")
    private Complemento complemento;

    /** UUID del timbre fiscal, o {@code null} si el CFDI no está timbrado. */
    public String getUuid() {
        TimbreFiscalDigital timbre = getTimbre();
        return timbre == null ? null : timbre.getUuid();
    }

    public TimbreFiscalDigital getTimbre() {
        return complemento == null ? null : complemento.getTimbreFiscalDigital();
    }

    public String getVersion() { return version; }
    public String getSerie() { return serie; }
    public String getFolio() { return folio; }
    public String getFecha() { return fecha; }
    public String getFormaPago() { return formaPago; }
    public BigDecimal getSubTotal() { return subTotal; }
    public BigDecimal getDescuento() { return descuento; }
    public String getMoneda() { return moneda; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public BigDecimal getTotal() { return total; }
    public String getTipoDeComprobante() { return tipoDeComprobante; }
    public String getMetodoPago() { return metodoPago; }
    public String getLugarExpedicion() { return lugarExpedicion; }
    public Emisor getEmisor() { return emisor; }
    public Receptor getReceptor() { return receptor; }
    public List<Concepto> getConceptos() { return conceptos == null ? List.of() : conceptos; }
    public Impuestos getImpuestos() { return impuestos; }
    public Complemento getComplemento() { return complemento; }
}
