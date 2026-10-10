package mx.portafolio.cfdibatch.proceso;

import mx.portafolio.cfdibatch.dominio.ComprobanteEntity;
import mx.portafolio.cfdibatch.dominio.ConceptoEntity;
import mx.portafolio.cfdibatch.dominio.ImpuestoEntity;
import mx.portafolio.cfdibatch.xml.Comprobante;
import mx.portafolio.cfdibatch.xml.Concepto;
import mx.portafolio.cfdibatch.xml.Impuesto;
import mx.portafolio.cfdibatch.xml.Impuestos;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

import static mx.portafolio.cfdibatch.proceso.CfdiValidator.nvl;

/** Convierte el modelo XML (ya validado) a entidades JPA. */
@Component
public class CfdiMapper {

    public ComprobanteEntity aEntidad(Comprobante c, String archivo, Long jobExecutionId) {
        ComprobanteEntity e = new ComprobanteEntity();
        e.setUuid(c.getUuid().toUpperCase(Locale.ROOT));
        e.setVersion(c.getVersion());
        e.setSerie(c.getSerie());
        e.setFolio(c.getFolio());
        e.setFecha(LocalDateTime.parse(c.getFecha()));
        e.setTipoComprobante(c.getTipoDeComprobante());
        e.setFormaPago(c.getFormaPago());
        e.setMetodoPago(c.getMetodoPago());
        e.setMoneda(c.getMoneda());
        e.setTipoCambio(c.getTipoCambio());
        e.setLugarExpedicion(c.getLugarExpedicion());

        Impuestos imp = c.getImpuestos();
        e.setSubtotal(c.getSubTotal());
        e.setDescuento(nvl(c.getDescuento()));
        e.setTotalTraslados(imp == null ? java.math.BigDecimal.ZERO : nvl(imp.getTotalImpuestosTrasladados()));
        e.setTotalRetenidos(imp == null ? java.math.BigDecimal.ZERO : nvl(imp.getTotalImpuestosRetenidos()));
        e.setTotal(c.getTotal());

        e.setEmisorRfc(c.getEmisor().getRfc());
        e.setEmisorNombre(c.getEmisor().getNombre());
        e.setEmisorRegimen(c.getEmisor().getRegimenFiscal());
        e.setReceptorRfc(c.getReceptor().getRfc());
        e.setReceptorNombre(c.getReceptor().getNombre());
        e.setReceptorUsoCfdi(c.getReceptor().getUsoCfdi());

        e.setFechaTimbrado(LocalDateTime.parse(c.getTimbre().getFechaTimbrado()));
        e.setArchivo(archivo);
        e.setJobExecutionId(jobExecutionId);
        e.setFechaCarga(LocalDateTime.now());

        c.getConceptos().forEach(concepto -> e.agregarConcepto(aConcepto(concepto)));
        if (imp != null) {
            imp.getTraslados().forEach(t -> e.agregarImpuesto(aImpuesto(t, ImpuestoEntity.Tipo.TRASLADO)));
            imp.getRetenciones().forEach(r -> e.agregarImpuesto(aImpuesto(r, ImpuestoEntity.Tipo.RETENCION)));
        }
        return e;
    }

    private ConceptoEntity aConcepto(Concepto c) {
        ConceptoEntity e = new ConceptoEntity();
        e.setClaveProdServ(c.getClaveProdServ());
        e.setNoIdentificacion(c.getNoIdentificacion());
        e.setCantidad(c.getCantidad());
        e.setClaveUnidad(c.getClaveUnidad());
        e.setDescripcion(c.getDescripcion());
        e.setValorUnitario(c.getValorUnitario());
        e.setImporte(c.getImporte());
        e.setDescuento(nvl(c.getDescuento()));
        e.setObjetoImp(c.getObjetoImp());
        return e;
    }

    private ImpuestoEntity aImpuesto(Impuesto i, ImpuestoEntity.Tipo tipo) {
        ImpuestoEntity e = new ImpuestoEntity();
        e.setTipo(tipo);
        e.setImpuesto(i.getImpuesto());
        e.setTipoFactor(i.getTipoFactor());
        e.setTasaOCuota(i.getTasaOCuota());
        e.setBase(i.getBase());
        e.setImporte(nvl(i.getImporte()));
        return e;
    }
}
