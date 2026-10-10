package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAttribute;

public class TimbreFiscalDigital {

    @XmlAttribute(name = "Version")
    private String version;
    @XmlAttribute(name = "UUID")
    private String uuid;
    @XmlAttribute(name = "FechaTimbrado")
    private String fechaTimbrado;
    @XmlAttribute(name = "RfcProvCertif")
    private String rfcProvCertif;
    @XmlAttribute(name = "NoCertificadoSAT")
    private String noCertificadoSat;

    public String getVersion() { return version; }
    public String getUuid() { return uuid; }
    public String getFechaTimbrado() { return fechaTimbrado; }
    public String getRfcProvCertif() { return rfcProvCertif; }
    public String getNoCertificadoSat() { return noCertificadoSat; }
}
