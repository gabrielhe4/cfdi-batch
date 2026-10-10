package mx.portafolio.cfdibatch.rechazo;

import mx.portafolio.cfdibatch.dominio.ComprobanteEntity;
import mx.portafolio.cfdibatch.lectura.CfdiArchivo;
import mx.portafolio.cfdibatch.lectura.CfdiIlegibleException;
import mx.portafolio.cfdibatch.proceso.CfdiInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Registra en la tabla cfdi_rechazado cada CFDI que se saltó, sin importar
 * en qué fase falló (lectura, proceso o escritura).
 */
public class CfdiSkipListener implements SkipListener<CfdiArchivo, ComprobanteEntity>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CfdiSkipListener.class);

    private final RechazoRepository rechazos;
    private Long jobExecutionId;

    public CfdiSkipListener(RechazoRepository rechazos) {
        this.rechazos = rechazos;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecutionId = stepExecution.getJobExecutionId();
    }

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof CfdiIlegibleException e) {
            log.warn("CFDI ilegible: {}", e.getMessage());
            rechazos.registrar(jobExecutionId, e.getArchivo(), null, e.getMessage());
        } else {
            log.warn("Error de lectura no identificado", t);
            rechazos.registrar(jobExecutionId, "(desconocido)", null, t.getMessage());
        }
    }

    @Override
    public void onSkipInProcess(CfdiArchivo item, Throwable t) {
        String uuid = t instanceof CfdiInvalidoException e ? e.getUuid() : item.comprobante().getUuid();
        String motivo = t instanceof CfdiInvalidoException e ? String.join("; ", e.getErrores()) : t.getMessage();
        log.warn("CFDI rechazado {}: {}", item.archivo(), motivo);
        rechazos.registrar(jobExecutionId, item.archivo(), uuid, motivo);
    }

    @Override
    public void onSkipInWrite(ComprobanteEntity item, Throwable t) {
        log.warn("No se pudo guardar {}: {}", item.getArchivo(), t.getMessage());
        rechazos.registrar(jobExecutionId, item.getArchivo(), item.getUuid(), "Error al guardar: " + t.getMessage());
    }
}
