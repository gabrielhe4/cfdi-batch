package mx.portafolio.cfdibatch;

import mx.portafolio.cfdibatch.lectura.CfdiArchivo;
import mx.portafolio.cfdibatch.lectura.CfdiIlegibleException;
import mx.portafolio.cfdibatch.lectura.CfdiXmlItemReader;
import mx.portafolio.cfdibatch.proceso.CfdiValidator;
import mx.portafolio.cfdibatch.xml.Comprobante;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CfdiValidatorTest {

    private static Jaxb2Marshaller marshaller;
    private final CfdiValidator validator = new CfdiValidator();

    @BeforeAll
    static void init() throws Exception {
        marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Comprobante.class);
        marshaller.afterPropertiesSet();
    }

    private static CfdiArchivo leer(String nombre) {
        CfdiXmlItemReader reader = new CfdiXmlItemReader(marshaller);
        reader.setResource(new ClassPathResource("cfdi/" + nombre));
        reader.open(new ExecutionContext());
        return reader.read();
    }

    @Test
    void leeCamposYComplementoTimbre() {
        Comprobante c = leer("CFDI-A002.xml").comprobante();

        assertThat(c.getVersion()).isEqualTo("4.0");
        assertThat(c.getEmisor().getRfc()).isEqualTo("GORL850412AB3");
        assertThat(c.getReceptor().getUsoCfdi()).isEqualTo("G03");
        assertThat(c.getConceptos()).hasSize(1);
        assertThat(c.getImpuestos().getRetenciones()).hasSize(2);
        assertThat(c.getImpuestos().getTotalImpuestosRetenidos()).isEqualByComparingTo(new BigDecimal("1963.34"));
        assertThat(c.getUuid()).isEqualTo("3A1C7D9E-2B4F-4E8A-9C61-7D2E5F0A8B13");
    }

    @Test
    void cfdiValidoNoTieneErrores() {
        for (String archivo : List.of("CFDI-A001.xml", "CFDI-A002.xml", "CFDI-A003.xml", "CFDI-A004.xml")) {
            assertThat(validator.validar(leer(archivo).comprobante()))
                    .as(archivo)
                    .isEmpty();
        }
    }

    @Test
    void detectaImportesQueNoCuadran() {
        List<String> errores = validator.validar(leer("CFDI-E001.xml").comprobante());

        assertThat(errores).hasSize(2);
        assertThat(errores).anyMatch(e -> e.contains("suma de importes de conceptos"));
        assertThat(errores).anyMatch(e -> e.startsWith("Total (1200.00)"));
    }

    @Test
    void detectaRfcInvalidoYFaltaDeTimbre() {
        List<String> errores = validator.validar(leer("CFDI-E004.xml").comprobante());

        assertThat(errores).anyMatch(e -> e.contains("RFC del receptor inválido"));
        assertThat(errores).anyMatch(e -> e.contains("sin TimbreFiscalDigital"));
    }

    @Test
    void xmlMalFormadoLanzaCfdiIlegibleYLuegoTermina() {
        CfdiXmlItemReader reader = new CfdiXmlItemReader(marshaller);
        reader.setResource(new ClassPathResource("cfdi/CFDI-E002.xml"));
        reader.open(new ExecutionContext());

        assertThatThrownBy(reader::read)
                .isInstanceOf(CfdiIlegibleException.class)
                .hasMessageContaining("CFDI-E002.xml");
        // El archivo queda consumido: el MultiResourceItemReader puede pasar al siguiente.
        assertThat(reader.read()).isNull();
    }
}
