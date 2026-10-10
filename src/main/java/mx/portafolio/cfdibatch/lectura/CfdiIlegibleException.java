package mx.portafolio.cfdibatch.lectura;

/** El archivo no es un XML bien formado o no es un CFDI 4.0. */
public class CfdiIlegibleException extends RuntimeException {

    private final String archivo;

    public CfdiIlegibleException(String archivo, Throwable causa) {
        super("No se pudo leer " + archivo + ": " + mensajeRaiz(causa), causa);
        this.archivo = archivo;
    }

    public String getArchivo() {
        return archivo;
    }

    private static String mensajeRaiz(Throwable t) {
        Throwable raiz = t;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        return raiz.getMessage();
    }
}
