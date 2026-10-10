package mx.portafolio.cfdibatch.config;

import jakarta.persistence.EntityManagerFactory;
import mx.portafolio.cfdibatch.archivado.ArchivadoTasklet;
import mx.portafolio.cfdibatch.dominio.ComprobanteEntity;
import mx.portafolio.cfdibatch.dominio.ComprobanteRepository;
import mx.portafolio.cfdibatch.lectura.CfdiArchivo;
import mx.portafolio.cfdibatch.lectura.CfdiIlegibleException;
import mx.portafolio.cfdibatch.lectura.CfdiXmlItemReader;
import mx.portafolio.cfdibatch.proceso.CfdiInvalidoException;
import mx.portafolio.cfdibatch.proceso.CfdiMapper;
import mx.portafolio.cfdibatch.proceso.CfdiProcessor;
import mx.portafolio.cfdibatch.proceso.CfdiValidator;
import mx.portafolio.cfdibatch.rechazo.CfdiSkipListener;
import mx.portafolio.cfdibatch.rechazo.RechazoRepository;
import mx.portafolio.cfdibatch.reporte.ResumenEmisor;
import mx.portafolio.cfdibatch.xml.Comprobante;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Job "cargaCfdiJob":
 * <ol>
 *   <li>cargaCfdiStep: lee XML → valida → guarda en BD (rechazos a tabla de errores)</li>
 *   <li>reporteStep: genera CSV con totales por emisor y mes de esta ejecución</li>
 *   <li>archivadoStep: mueve los XML a procesados/ o rechazados/</li>
 * </ol>
 *
 * Nota: en Spring Boot 3 NO se usa @EnableBatchProcessing, porque desactiva la
 * autoconfiguración (JobRepository, ejecución al arrancar, etc.).
 */
@Configuration
public class BatchConfig {

    private static final String SQL_RESUMEN = """
            SELECT emisor_rfc, emisor_nombre,
                   EXTRACT(YEAR FROM fecha)  AS anio,
                   EXTRACT(MONTH FROM fecha) AS mes,
                   COUNT(*)                  AS cantidad,
                   SUM(subtotal - descuento) AS subtotal_neto,
                   SUM(total_traslados)      AS trasladados,
                   SUM(total_retenidos)      AS retenidos,
                   SUM(total)                AS total
            FROM comprobante
            WHERE job_execution_id = ?
            GROUP BY emisor_rfc, emisor_nombre, EXTRACT(YEAR FROM fecha), EXTRACT(MONTH FROM fecha)
            ORDER BY emisor_rfc, anio, mes
            """;

    // ---------------------------------------------------------------- Job

    @Bean
    public Job cargaCfdiJob(JobRepository jobRepository,
                            Step cargaCfdiStep, Step reporteStep, Step archivadoStep) {
        return new JobBuilder("cargaCfdiJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cargaCfdiStep)
                .next(reporteStep)
                .next(archivadoStep)
                .build();
    }

    // ---------------------------------------------------------------- Step 1: carga

    @Bean
    public Step cargaCfdiStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              CfdiProperties props,
                              MultiResourceItemReader<CfdiArchivo> cfdiReader,
                              CfdiProcessor cfdiProcessor,
                              JpaItemWriter<ComprobanteEntity> cfdiWriter,
                              RechazoRepository rechazoRepository) {

        CfdiSkipListener skipListener = new CfdiSkipListener(rechazoRepository);

        return new StepBuilder("cargaCfdiStep", jobRepository)
                .<CfdiArchivo, ComprobanteEntity>chunk(props.chunkSize(), transactionManager)
                .reader(cfdiReader)
                .processor(cfdiProcessor)
                .writer(cfdiWriter)
                .faultTolerant()
                .skipLimit(props.skipLimit())
                .skip(CfdiIlegibleException.class)
                .skip(CfdiInvalidoException.class)
                // Un CFDI inválido no debe deshacer el chunk completo: solo se salta.
                .noRollback(CfdiInvalidoException.class)
                // Si un write falla y Batch reprocesa el chunk ítem por ítem, reutiliza
                // el resultado del processor en lugar de volver a llamarlo (evita falsos
                // "UUID duplicado" con el set de UUIDs vistos).
                .processorNonTransactional()
                .listener((SkipListener<CfdiArchivo, ComprobanteEntity>) skipListener)
                .listener((StepExecutionListener) skipListener)
                .build();
    }

    @Bean
    public Jaxb2Marshaller cfdiMarshaller() {
        // Por defecto Jaxb2Marshaller no procesa entidades externas ni DTD (protección XXE).
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Comprobante.class);
        return marshaller;
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<CfdiArchivo> cfdiReader(CfdiProperties props,
                                                           Jaxb2Marshaller cfdiMarshaller) throws IOException {
        Files.createDirectories(props.entrada());
        Resource[] archivos;
        try (Stream<Path> s = Files.list(props.entrada())) {
            archivos = s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                    .sorted()
                    .map(FileSystemResource::new)
                    .toArray(Resource[]::new);
        }
        return new MultiResourceItemReaderBuilder<CfdiArchivo>()
                .name("cfdiReader")
                .resources(archivos)
                .delegate(new CfdiXmlItemReader(cfdiMarshaller))
                .build();
    }

    @Bean
    @StepScope
    public CfdiProcessor cfdiProcessor(CfdiValidator validator,
                                       CfdiMapper mapper,
                                       ComprobanteRepository repository,
                                       @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {
        return new CfdiProcessor(validator, mapper, repository, jobExecutionId);
    }

    @Bean
    public JpaItemWriter<ComprobanteEntity> cfdiWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<ComprobanteEntity>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    // ---------------------------------------------------------------- Step 2: reporte

    @Bean
    public Step reporteStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            JdbcCursorItemReader<ResumenEmisor> resumenReader,
                            FlatFileItemWriter<ResumenEmisor> resumenWriter) {
        return new StepBuilder("reporteStep", jobRepository)
                .<ResumenEmisor, ResumenEmisor>chunk(100, transactionManager)
                .reader(resumenReader)
                .writer(resumenWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<ResumenEmisor> resumenReader(
            DataSource dataSource,
            @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {
        return new JdbcCursorItemReaderBuilder<ResumenEmisor>()
                .name("resumenReader")
                .dataSource(dataSource)
                .sql(SQL_RESUMEN)
                .preparedStatementSetter(ps -> ps.setLong(1, jobExecutionId))
                .rowMapper((rs, n) -> new ResumenEmisor(
                        rs.getString("emisor_rfc"),
                        rs.getString("emisor_nombre"),
                        rs.getInt("anio"),
                        rs.getInt("mes"),
                        rs.getLong("cantidad"),
                        rs.getBigDecimal("subtotal_neto"),
                        rs.getBigDecimal("trasladados"),
                        rs.getBigDecimal("retenidos"),
                        rs.getBigDecimal("total")))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ResumenEmisor> resumenWriter(
            CfdiProperties props,
            @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) throws IOException {
        Files.createDirectories(props.reportes());
        Path salida = props.reportes().resolve("resumen-emisores-" + jobExecutionId + ".csv");
        return new FlatFileItemWriterBuilder<ResumenEmisor>()
                .name("resumenWriter")
                .resource(new FileSystemResource(salida))
                .encoding("UTF-8")
                .headerCallback(w -> w.write(ResumenEmisor.ENCABEZADO))
                .lineAggregator(ResumenEmisor::aCsv)
                .build();
    }

    // ---------------------------------------------------------------- Step 3: archivado

    @Bean
    public Step archivadoStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              CfdiProperties props,
                              RechazoRepository rechazoRepository) {
        return new StepBuilder("archivadoStep", jobRepository)
                .tasklet(new ArchivadoTasklet(props, rechazoRepository), transactionManager)
                .build();
    }
}
