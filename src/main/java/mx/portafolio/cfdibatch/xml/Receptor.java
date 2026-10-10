package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Receptor {

    @XmlAttribute(name = "Rfc")
    private String rfc;
    @XmlAttribute(name = "Nombre")
    private String nombre;
    @XmlAttribute(name = "DomicilioFiscalReceptor")
    private String domicilioFiscalReceptor;
    @XmlAttribute(name = "RegimenFiscalReceptor")
    private String regimenFiscalReceptor;
    @XmlAttribute(name = "UsoCFDI")
    private String usoCfdi;

    public String getRfc() { return rfc; }
    public String getNombre() { return nombre; }
    public String getDomicilioFiscalReceptor() { return domicilioFiscalReceptor; }
    public String getRegimenFiscalReceptor() { return regimenFiscalReceptor; }
    public String getUsoCfdi() { return usoCfdi; }
}
