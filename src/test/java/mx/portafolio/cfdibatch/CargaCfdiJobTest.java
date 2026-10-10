package mx.portafolio.cfdibatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de punta a punta: 8 XML de entrada (4 válidos, 4 con distintos errores).
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class CargaCfdiJobTest {

    private static final Path BASE;

    static {
        try {
            BASE = Files.createTempDirectory("cfdi-batch-it");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void directorios(DynamicPropertyRegistry registry) {
        registry.add("cfdi.entrada", () -> BASE.resolve("entrada").toString());
        registry.add("cfdi.procesados", () -> BASE.resolve("procesados").toString());
        registry.add("cfdi.rechazados", () -> BASE.resolve("rechazados").toString());
        registry.add("cfdi.reportes", () -> BASE.resolve("reportes").toString());
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void copiarMuestras() throws IOException {
        Path entrada = Files.createDirectories(BASE.resolve("entrada"));
        Resource[] muestras = new PathMatchingResourcePatternResolver().getResources("classpath:cfdi/*.xml");
        for (Resource r : muestras) {
            try (InputStream in = r.getInputStream()) {
                Files.copy(in, entrada.resolve(r.getFilename()));
            }
        }
    }

    @Test
    void cargaValidosRechazaInvalidosYGeneraReporte() throws Exception {
        JobExecution ejecucion = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        assertThat(ejecucion.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        StepExecution carga = ejecucion.getStepExecutions().stream()
                .filter(s -> s.getStepName().equals("cargaCfdiStep"))
                .findFirst().orElseThrow();
        assertThat(carga.getWriteCount()).isEqualTo(4);
        assertThat(carga.getReadSkipCount()).isEqualTo(1);     // E002 mal formado
        assertThat(carga.getProcessSkipCount()).isEqualTo(3);  // E001, E003, E004

        // BD
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM comprobante", Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM concepto", Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForList("SELECT archivo FROM cfdi_rechazado ORDER BY archivo", String.class))
                .containsExactly("CFDI-E001.xml", "CFDI-E002.xml", "CFDI-E003.xml", "CFDI-E004.xml");
        assertThat(jdbc.queryForObject(
                "SELECT motivo FROM cfdi_rechazado WHERE archivo = 'CFDI-E003.xml'", String.class))
                .contains("UUID duplicado");

        // Archivos movidos
        assertThat(listar("entrada")).isEmpty();
        assertThat(listar("procesados")).containsExactly(
                "CFDI-A001.xml", "CFDI-A002.xml", "CFDI-A003.xml", "CFDI-A004.xml");
        assertThat(listar("rechazados")).containsExactly(
                "CFDI-E001.xml", "CFDI-E002.xml", "CFDI-E003.xml", "CFDI-E004.xml");

        // Reporte: encabezado + 3 filas (EKU sep, GORL sep, CNO oct)
        Path csv = BASE.resolve("reportes").resolve("resumen-emisores-" + ejecucion.getId() + ".csv");
        List<String> lineas = Files.readAllLines(csv);
        assertThat(lineas).hasSize(4);
        assertThat(lineas).contains("EKU9003173C9,\"ESCUELA KEMPER URGATE\",2026,09,2,4400.00,704.00,0.00,5104.00");
    }

    private static List<String> listar(String carpeta) throws IOException {
        try (var s = Files.list(BASE.resolve(carpeta))) {
            return s.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }
}
