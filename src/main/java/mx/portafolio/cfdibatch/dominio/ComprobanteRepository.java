package mx.portafolio.cfdibatch.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<ComprobanteEntity, Long> {

    boolean existsByUuid(String uuid);
}
