package mx.portafolio.cfdibatch.lectura;

import mx.portafolio.cfdibatch.xml.Comprobante;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * Lee UN archivo CFDI completo y lo convierte en un {@link CfdiArchivo}.
 *
 * <p>Se usa como delegado de {@code MultiResourceItemReader}: cada XML es un documento
 * independiente, así que no hace falta streaming por fragmentos (StaxEventItemReader),
 * que conviene más cuando un solo archivo enorme contiene muchos registros.</p>
 *
 * <p>Si el XML está mal formado lanza {@link CfdiIlegibleException}; como el archivo
 * ya quedó marcado como leído, la siguiente llamada devuelve {@code null} y el
 * MultiResourceItemReader avanza al siguiente archivo. Así el skip-on-read es seguro.</p>
 */
public class CfdiXmlItemReader implements ResourceAwareItemReaderItemStream<CfdiArchivo> {

    private final Unmarshaller unmarshaller;
    private Resource resource;
    private boolean leido;

    public CfdiXmlItemReader(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        this.leido = false;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        this.leido = false;
    }

    @Override
    public CfdiArchivo read() {
        if (leido || resource == null) {
            return null;
        }
        leido = true;
        String archivo = resource.getFilename();
        try (InputStream in = resource.getInputStream()) {
            Object raiz = unmarshaller.unmarshal(new StreamSource(in));
            if (!(raiz instanceof Comprobante comprobante)) {
                throw new IllegalStateException("El nodo raíz no es cfdi:Comprobante 4.0");
            }
            return new CfdiArchivo(archivo, comprobante);
        } catch (Exception e) {
            throw new CfdiIlegibleException(archivo, e);
        }
    }
}
