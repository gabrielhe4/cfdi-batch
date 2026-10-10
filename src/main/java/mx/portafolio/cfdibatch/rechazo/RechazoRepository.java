package mx.portafolio.cfdibatch.rechazo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RechazoRepository {

    private static final int MAX_MOTIVO = 2000;

    private final JdbcTemplate jdbc;

    public RechazoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(Long jobExecutionId, String archivo, String uuid, String motivo) {
        String recortado = motivo == null ? null
                : motivo.length() > MAX_MOTIVO ? motivo.substring(0, MAX_MOTIVO) : motivo;
        jdbc.update("""
                INSERT INTO cfdi_rechazado (job_execution_id, archivo, uuid, motivo, fecha)
                VALUES (?, ?, ?, ?, ?)
                """, jobExecutionId, archivo, uuid, recortado, Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<String> archivosRechazados(Long jobExecutionId) {
        return jdbc.queryForList(
                "SELECT DISTINCT archivo FROM cfdi_rechazado WHERE job_execution_id = ?",
                String.class, jobExecutionId);
    }
}
