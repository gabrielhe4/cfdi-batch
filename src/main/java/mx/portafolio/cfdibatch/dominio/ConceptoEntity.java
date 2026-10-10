package mx.portafolio.cfdibatch.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "concepto")
public class ConceptoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concepto_seq")
    @SequenceGenerator(name = "concepto_seq", sequenceName = "concepto_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private ComprobanteEntity comprobante;

    private String claveProdServ;
    private String noIdentificacion;
    private BigDecimal cantidad;
    private String claveUnidad;
    private String descripcion;
    private BigDecimal valorUnitario;
    private BigDecimal importe;
    private BigDecimal descuento;
    private String objetoImp;

    void setComprobante(ComprobanteEntity comprobante) { this.comprobante = comprobante; }
    public void setClaveProdServ(String claveProdServ) { this.claveProdServ = claveProdServ; }
    public void setNoIdentificacion(String noIdentificacion) { this.noIdentificacion = noIdentificacion; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public void setClaveUnidad(String claveUnidad) { this.claveUnidad = claveUnidad; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public void setObjetoImp(String objetoImp) { this.objetoImp = objetoImp; }
}
