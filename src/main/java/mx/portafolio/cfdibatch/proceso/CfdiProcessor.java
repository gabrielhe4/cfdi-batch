package mx.portafolio.cfdibatch.proceso;

import mx.portafolio.cfdibatch.dominio.ComprobanteEntity;
import mx.portafolio.cfdibatch.dominio.ComprobanteRepository;
import mx.portafolio.cfdibatch.lectura.CfdiArchivo;
import org.springframework.batch.item.ItemProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Valida el CFDI, detecta UUIDs duplicados y lo transforma a entidad.
 *
 * <p>Es step-scoped: el set de UUIDs vistos vive solo durante la ejecución del step,
 * para detectar duplicados que todavía no se han escrito en la BD (mismo chunk).</p>
 */
public class CfdiProcessor implements ItemProcessor<CfdiArchivo, ComprobanteEntity> {

    private final CfdiValidator validator;
    private final CfdiMapper mapper;
    private final ComprobanteRepository repository;
    private final Long jobExecutionId;
    private final Set<String> uuidsVistos = new HashSet<>();

    public CfdiProcessor(CfdiValidator validator, CfdiMapper mapper,
                         ComprobanteRepository repository, Long jobExecutionId) {
        this.validator = validator;
        this.mapper = mapper;
        this.repository = repository;
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public ComprobanteEntity process(CfdiArchivo item) {
        var comprobante = item.comprobante();
        List<String> errores = validator.validar(comprobante);

        String uuid = comprobante.getUuid() == null ? null : comprobante.getUuid().toUpperCase(Locale.ROOT);
        if (errores.isEmpty()) {
            if (repository.existsByUuid(uuid)) {
                errores = List.of("UUID duplicado (ya existe en la base de datos): " + uuid);
            } else if (!uuidsVistos.add(uuid)) {
                errores = List.of("UUID duplicado (repetido en esta misma ejecución): " + uuid);
            }
        }

        if (!errores.isEmpty()) {
            throw new CfdiInvalidoException(item.archivo(), uuid, errores);
        }
        return mapper.aEntidad(comprobante, item.archivo(), jobExecutionId);
    }
}
