# cfdi-batch

Carga y validación masiva de **CFDI 4.0** (facturas electrónicas del SAT en formato XML) con **Spring Batch**.

Toma una carpeta con archivos XML, valida cada comprobante contra reglas de negocio, guarda los válidos en PostgreSQL, registra los rechazados con su motivo, genera un reporte CSV de totales por emisor y mes, y archiva los archivos procesados.

## Stack

- Java 17, Spring Boot 3.5, Spring Batch 5
- JAXB (Spring OXM) para leer el XML
- Spring Data JPA / Hibernate, PostgreSQL 16, Flyway
- JUnit 5, `spring-batch-test`, H2 para pruebas

## Flujo del job `cargaCfdiJob`

```
data/entrada/*.xml
        │
        ▼
┌──────────────────────── cargaCfdiStep (chunk, fault tolerant) ────────────────────────┐
│ MultiResourceItemReader ──► CfdiProcessor ──────────────► JpaItemWriter               │
│  └ CfdiXmlItemReader (JAXB)   ├ CfdiValidator (reglas)      └ comprobante / concepto  │
│                               ├ UUID duplicado (BD + lote)    / impuesto              │
│                               └ CfdiMapper (XML → JPA)                                │
│                                                                                       │
│ XML ilegible (skip en read) ─┐                                                        │
│ CFDI inválido (skip en process) ─► CfdiSkipListener ──► tabla cfdi_rechazado          │
└───────────────────────────────────────────────────────────────────────────────────────┘
        │
        ▼
reporteStep:   JdbcCursorItemReader (GROUP BY emisor, mes) ──► FlatFileItemWriter (CSV)
        │
        ▼
archivadoStep: Tasklet ──► data/procesados/  |  data/rechazados/
```

### Reglas de validación

| Regla | Ejemplo de rechazo |
|---|---|
| Versión 4.0 | CFDI 3.3 |
| XML bien formado y raíz `cfdi:Comprobante` 4.0 | archivo truncado |
| Formato de RFC de emisor y receptor | `INVALIDO123` |
| Timbre fiscal presente con UUID válido | CFDI sin timbrar |
| Σ importes de conceptos = `SubTotal` | conceptos suman 950, SubTotal 1000 |
| `Total = SubTotal − Descuento + Trasladados − Retenidos` | Total 1200, esperado 1160 |
| Totales de impuestos = suma de traslados / retenciones | |
| UUID no repetido (en BD ni en la misma ejecución) | factura cargada dos veces |

El validador regresa **todos** los errores de un comprobante, no solo el primero.

## Cómo ejecutarlo

```bash
# 1. Base de datos
docker compose up -d

# 2. Copiar los CFDI de ejemplo a la carpeta de entrada
mkdir -p data/entrada
cp src/test/resources/cfdi/*.xml data/entrada/

# 3. Ejecutar el job
./mvnw spring-boot:run        # o: mvn spring-boot:run
```

> Si no tienes el wrapper, genéralo una vez con `mvn wrapper:wrapper`.

Resultado esperado con los ejemplos: 4 CFDI cargados, 4 rechazados, el reporte en `data/reportes/resumen-emisores-<id>.csv` y los XML movidos a `data/procesados/` y `data/rechazados/`.

Consultas útiles:

```sql
SELECT archivo, motivo FROM cfdi_rechazado ORDER BY id;
SELECT emisor_rfc, COUNT(*), SUM(total) FROM comprobante GROUP BY emisor_rfc;
SELECT step_name, read_count, write_count, read_skip_count, process_skip_count
FROM batch_step_execution ORDER BY step_execution_id DESC;
```

Los directorios, el tamaño de chunk y el límite de skips se configuran en `application.yml` (prefijo `cfdi`) o por línea de comandos, por ejemplo `--cfdi.entrada=/ruta/xml`.

## Pruebas

```bash
mvn test
```

- `CfdiValidatorTest`: lectura JAXB y cada regla de validación.
- `CargaCfdiJobTest`: el job completo de punta a punta con H2 (conteos, rechazos, archivos movidos y contenido del CSV).

## Datos de ejemplo

Los XML en `src/test/resources/cfdi/` son **sintéticos** y no tienen validez fiscal (sellos y certificados son texto de relleno). No subas CFDI reales a un repositorio: contienen RFC, nombres y montos de personas y empresas.

| Archivo | Caso |
|---|---|
| CFDI-A001 … A004 | Válidos (incluye honorarios con descuento y retenciones de ISR/IVA) |
| CFDI-E001 | Importes que no cuadran (2 errores) |
| CFDI-E002 | XML mal formado |
| CFDI-E003 | UUID duplicado de A001 |
| CFDI-E004 | RFC inválido y sin timbre |

## Decisiones técnicas

- **Un archivo = un ítem.** Cada CFDI es un documento XML independiente, así que se usa `MultiResourceItemReader` con un delegado que deserializa el archivo completo con JAXB. `StaxEventItemReader` conviene más cuando un solo archivo grande contiene muchos registros.
- **Skip seguro en lectura.** El delegado marca el archivo como consumido antes de parsearlo; si el XML está roto lanza `CfdiIlegibleException` y la siguiente lectura pasa al archivo siguiente.
- **`noRollback` + `processorNonTransactional`.** Un CFDI inválido se salta sin deshacer el chunk, y si un write falla y Batch reprocesa ítem por ítem, no vuelve a ejecutar el processor (que guarda estado de UUIDs vistos).
- **Inserts en lote.** Las entidades usan secuencias con `allocationSize = 50` y `hibernate.jdbc.batch_size`; con `IDENTITY` Hibernate desactiva el batching.
- **Archivado al final.** Los archivos se mueven en el último step para que, si el job falla y se reinicia, la carga vuelva a encontrar los mismos archivos.
- **Seguridad XML.** `Jaxb2Marshaller` no procesa DTD ni entidades externas por defecto (protección contra XXE).
- Sin `@EnableBatchProcessing`: en Spring Boot 3 desactiva la autoconfiguración.

## Siguientes pasos

- [ ] Generador de CFDI sintéticos para pruebas de volumen (miles de archivos).
- [ ] Partitioning para procesar archivos en paralelo.
- [ ] API REST para lanzar el job y consultar ejecuciones.
- [ ] Validación contra el XSD oficial del SAT y consulta de estatus del CFDI.
- [ ] Pruebas con Testcontainers (PostgreSQL real) en lugar de H2.
