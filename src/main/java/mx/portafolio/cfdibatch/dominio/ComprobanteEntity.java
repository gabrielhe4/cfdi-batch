package mx.portafolio.cfdibatch.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comprobante")
public class ComprobanteEntity {

    // Secuencia con allocationSize = 50 para que Hibernate pueda hacer inserts en lote
    // (con IDENTITY el batching de JDBC se desactiva).
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comprobante_seq")
    @SequenceGenerator(name = "comprobante_seq", sequenceName = "comprobante_seq", allocationSize = 50)
    private Long id;

    private String uuid;
    private String version;
    private String serie;
    private String folio;
    private LocalDateTime fecha;
    private String tipoComprobante;
    private String formaPago;
    private String metodoPago;
    private String moneda;
    private BigDecimal tipoCambio;
    private String lugarExpedicion;

    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal totalTraslados;
    private BigDecimal totalRetenidos;
    private BigDecimal total;

    private String emisorRfc;
    private String emisorNombre;
    private String emisorRegimen;
    private String receptorRfc;
    private String receptorNombre;
    private String receptorUsoCfdi;

    private LocalDateTime fechaTimbrado;
    private String archivo;
    private Long jobExecutionId;
    private LocalDateTime fechaCarga;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConceptoEntity> conceptos = new ArrayList<>();

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImpuestoEntity> impuestos = new ArrayList<>();

    public void agregarConcepto(ConceptoEntity concepto) {
        concepto.setComprobante(this);
        conceptos.add(concepto);
    }

    public void agregarImpuesto(ImpuestoEntity impuesto) {
        impuesto.setComprobante(this);
        impuestos.add(impuesto);
    }

    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getArchivo() { return archivo; }
    public BigDecimal getTotal() { return total; }
    public List<ConceptoEntity> getConceptos() { return conceptos; }
    public List<ImpuestoEntity> getImpuestos() { return impuestos; }

    public void setUuid(String uuid) { this.uuid = uuid; }
    public void setVersion(String version) { this.version = version; }
    public void setSerie(String serie) { this.serie = serie; }
    public void setFolio(String folio) { this.folio = folio; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
    public void setLugarExpedicion(String lugarExpedicion) { this.lugarExpedicion = lugarExpedicion; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public void setTotalTraslados(BigDecimal totalTraslados) { this.totalTraslados = totalTraslados; }
    public void setTotalRetenidos(BigDecimal totalRetenidos) { this.totalRetenidos = totalRetenidos; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setEmisorRfc(String emisorRfc) { this.emisorRfc = emisorRfc; }
    public void setEmisorNombre(String emisorNombre) { this.emisorNombre = emisorNombre; }
    public void setEmisorRegimen(String emisorRegimen) { this.emisorRegimen = emisorRegimen; }
    public void setReceptorRfc(String receptorRfc) { this.receptorRfc = receptorRfc; }
    public void setReceptorNombre(String receptorNombre) { this.receptorNombre = receptorNombre; }
    public void setReceptorUsoCfdi(String receptorUsoCfdi) { this.receptorUsoCfdi = receptorUsoCfdi; }
    public void setFechaTimbrado(LocalDateTime fechaTimbrado) { this.fechaTimbrado = fechaTimbrado; }
    public void setArchivo(String archivo) { this.archivo = archivo; }
    public void setJobExecutionId(Long jobExecutionId) { this.jobExecutionId = jobExecutionId; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
}
