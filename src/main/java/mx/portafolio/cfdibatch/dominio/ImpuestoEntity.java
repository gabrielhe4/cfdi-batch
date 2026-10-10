package mx.portafolio.cfdibatch.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Impuesto a nivel comprobante (resumen de traslados y retenciones). */
@Entity
@Table(name = "impuesto")
public class ImpuestoEntity {

    public enum Tipo { TRASLADO, RETENCION }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "impuesto_seq")
    @SequenceGenerator(name = "impuesto_seq", sequenceName = "impuesto_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private ComprobanteEntity comprobante;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    private String impuesto;
    private String tipoFactor;
    // Explícito: la naming strategy de Spring convierte "tasaOCuota" en "tasaocuota"
    // (no separa dos mayúsculas seguidas).
    @Column(name = "tasa_o_cuota")
    private BigDecimal tasaOCuota;
    private BigDecimal base;
    private BigDecimal importe;

    void setComprobante(ComprobanteEntity comprobante) { this.comprobante = comprobante; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public void setImpuesto(String impuesto) { this.impuesto = impuesto; }
    public void setTipoFactor(String tipoFactor) { this.tipoFactor = tipoFactor; }
    public void setTasaOCuota(BigDecimal tasaOCuota) { this.tasaOCuota = tasaOCuota; }
    public void setBase(BigDecimal base) { this.base = base; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}