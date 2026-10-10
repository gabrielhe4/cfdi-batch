package mx.portafolio.cfdibatch.archivado;

import mx.portafolio.cfdibatch.config.CfdiProperties;
import mx.portafolio.cfdibatch.rechazo.RechazoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Último paso: mueve cada XML de la carpeta de entrada a procesados/ o rechazados/.
 * Se hace al final (y no durante la carga) para que un reinicio del job
 * vuelva a encontrar los mismos archivos.
 */
public class ArchivadoTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ArchivadoTasklet.class);

    private final CfdiProperties props;
    private final RechazoRepository rechazos;

    public ArchivadoTasklet(CfdiProperties props, RechazoRepository rechazos) {
        this.props = props;
        this.rechazos = rechazos;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws IOException {
        Long jobExecutionId = contribution.getStepExecution().getJobExecutionId();
        Set<String> rechazados = new HashSet<>(rechazos.archivosRechazados(jobExecutionId));

        Files.createDirectories(props.procesados());
        Files.createDirectories(props.rechazados());

        List<Path> archivos;
        try (Stream<Path> s = Files.list(props.entrada())) {
            archivos = s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml")).toList();
        }

        int ok = 0;
        int ko = 0;
        for (Path archivo : archivos) {
            String nombre = archivo.getFileName().toString();
            boolean rechazado = rechazados.contains(nombre);
            Path destino = (rechazado ? props.rechazados() : props.procesados()).resolve(nombre);
            Files.move(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
            if (rechazado) ko++; else ok++;
            contribution.incrementWriteCount(1);
        }
        log.info("Archivado terminado: {} procesados, {} rechazados", ok, ko);
        return RepeatStatus.FINISHED;
    }
}
