package mx.portafolio.cfdibatch.proceso;

import java.util.List;

/** El CFDI se leyó bien pero no pasó las reglas de negocio. */
public class CfdiInvalidoException extends RuntimeException {

    private final String archivo;
    private final String uuid;
    private final List<String> errores;

    public CfdiInvalidoException(String archivo, String uuid, List<String> errores) {
        super(archivo + ": " + String.join("; ", errores));
        this.archivo = archivo;
        this.uuid = uuid;
        this.errores = List.copyOf(errores);
    }

    public String getArchivo() { return archivo; }
    public String getUuid() { return uuid; }
    public List<String> getErrores() { return errores; }
}
