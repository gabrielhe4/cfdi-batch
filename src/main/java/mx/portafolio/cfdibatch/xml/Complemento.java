package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * Complemento del CFDI. Solo se mapea el TimbreFiscalDigital;
 * cualquier otro complemento (pagos, nómina, etc.) se ignora.
 */
public class Complemento {

    @XmlElement(name = "TimbreFiscalDigital", namespace = CfdiNamespaces.TFD)
    private TimbreFiscalDigital timbreFiscalDigital;

    public TimbreFiscalDigital getTimbreFiscalDigital() { return timbreFiscalDigital; }
}
