package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.math.BigDecimal;

public class Concepto {

    @XmlAttribute(name = "ClaveProdServ")
    private String claveProdServ;
    @XmlAttribute(name = "NoIdentificacion")
    private String noIdentificacion;
    @XmlAttribute(name = "Cantidad")
    private BigDecimal cantidad;
    @XmlAttribute(name = "ClaveUnidad")
    private String claveUnidad;
    @XmlAttribute(name = "Unidad")
    private String unidad;
    @XmlAttribute(name = "Descripcion")
    private String descripcion;
    @XmlAttribute(name = "ValorUnitario")
    private BigDecimal valorUnitario;
    @XmlAttribute(name = "Importe")
    private BigDecimal importe;
    @XmlAttribute(name = "Descuento")
    private BigDecimal descuento;
    @XmlAttribute(name = "ObjetoImp")
    private String objetoImp;

    @XmlElement(name = "Impuestos")
    private Impuestos impuestos;

    public String getClaveProdServ() { return claveProdServ; }
    public String getNoIdentificacion() { return noIdentificacion; }
    public BigDecimal getCantidad() { return cantidad; }
    public String getClaveUnidad() { return claveUnidad; }
    public String getUnidad() { return unidad; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getImporte() { return importe; }
    public BigDecimal getDescuento() { return descuento; }
    public String getObjetoImp() { return objetoImp; }
    public Impuestos getImpuestos() { return impuestos; }
}
