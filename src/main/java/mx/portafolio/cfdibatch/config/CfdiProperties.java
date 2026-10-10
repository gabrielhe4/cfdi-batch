package mx.portafolio.cfdibatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Directorios de trabajo y parámetros de ejecución del job.
 */
@ConfigurationProperties(prefix = "cfdi")
public record CfdiProperties(
        Path entrada,
        Path procesados,
        Path rechazados,
        Path reportes,
        int chunkSize,
        int skipLimit) {
}
