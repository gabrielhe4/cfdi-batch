/**
 * Modelo JAXB (subconjunto) del CFDI 4.0 del SAT.
 * Se mapean solo los nodos que el proceso necesita; JAXB ignora el resto
 * (Sello, Certificado, otros complementos, etc.).
 */
@XmlSchema(namespace = CfdiNamespaces.CFDI_40, elementFormDefault = XmlNsForm.QUALIFIED)
@XmlAccessorType(XmlAccessType.FIELD)
package mx.portafolio.cfdibatch.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
