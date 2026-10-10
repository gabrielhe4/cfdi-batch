package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Emisor {

    @XmlAttribute(name = "Rfc")
    private String rfc;
    @XmlAttribute(name = "Nombre")
    private String nombre;
    @XmlAttribute(name = "RegimenFiscal")
    private String regimenFiscal;

    public String getRfc() { return rfc; }
    public String getNombre() { return nombre; }
    public String getRegimenFiscal() { return regimenFiscal; }
}
