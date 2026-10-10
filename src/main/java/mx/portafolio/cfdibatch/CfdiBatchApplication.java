package mx.portafolio.cfdibatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CfdiBatchApplication {

    public static void main(String[] args) {
        // Termina con el exit code del job (0 = COMPLETED), útil para cron / schedulers.
        System.exit(SpringApplication.exit(SpringApplication.run(CfdiBatchApplication.class, args)));
    }
}
