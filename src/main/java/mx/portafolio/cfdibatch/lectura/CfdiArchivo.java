package mx.portafolio.cfdibatch.lectura;

import mx.portafolio.cfdibatch.xml.Comprobante;

/** Un CFDI leído junto con el nombre del archivo de donde salió. */
public record CfdiArchivo(String archivo, Comprobante comprobante) {
}
