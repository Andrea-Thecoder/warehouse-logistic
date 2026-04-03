# Convenzioni di Codice

## Naming

### Classi

| Tipo | Suffisso | Esempio |
|------|---------|---------|
| Controller REST | `Resource` | `WarehouseResource` |
| Service | `Service` | `StockService` |
| Entità persistente | nessuno | `Warehouse`, `Product` |
| DTO inserimento | `InsertXxxDTO` | `InsertWarehouseDTO` |
| DTO risposta base | `BaseDetailXxxDTO` | `BaseDetailWarehouseDTO` |
| DTO risposta completa | `DetailXxxDTO` | `DetailMovementTrackDTO` |
| DTO risposta semplificata | `SimpleDetailXxxDTO` | `SimpleDetailWarehouseDTO` |
| Search request | `XxxSearchRequest` | `WarehouseSearchRequest` |
| Enum | PascalCase | `MovementStatus`, `StockAction` |
| Eccezione custom | `ServiceException` | — |

### Campi di Audit
I campi di audit in `AbstractAuditable` usano il prefisso `_` e camelCase:
```java
_dataCreazione, _utenteCreazione, _dataModifica, _utenteModifica, _version
```
Questa convenzione distingue visivamente i metadati tecnici dai campi di dominio.

### Package
Tutto sotto `it.warehouse.optimization.*`. I sotto-package corrispondono ai layer.

---

## Pattern DTO

Ogni dominio ha una tripletta di DTO:

1. **`InsertXxxDTO`** — input per POST/PUT. Contiene validazioni Jakarta (`@NotNull`, `@Min`, ecc.).
2. **`BaseDetailXxxDTO`** — campi minimi per le liste. Usato in `PagedResultDTO`.
3. **`DetailXxxDTO`** o **`SimpleDetailXxxDTO`** — risposta completa per dettaglio singolo.

Non si espongono mai le entità del model direttamente nelle API.

---

## Pattern Service

- Ogni Service è `@ApplicationScoped`
- Riceve in `@Inject` il `Database` di Ebean e gli altri Service necessari
- Lancia `ServiceException` per errori di dominio (con status HTTP e messaggio leggibile)
- Non contiene logica HTTP (nessun riferimento a `Response`, `HttpServletRequest`, ecc.)
- Gestisce esplicitamente le transazioni con `db.beginTransaction()` / `tx.commit()`

```java
@ApplicationScoped
public class StockService {

    @Inject
    Database db;

    public SimpleResultDTO<BaseDetailStockDTO> createStock(InsertStockDTO dto) {
        try (Transaction tx = db.beginTransaction()) {
            // logica...
            tx.commit();
            return new SimpleResultDTO<>(result, "Stock creato");
        }
    }
}
```

---

## Pattern Resource (Controller)

- Ogni Resource è `@Path`-annotato con il path della risorsa
- Usa `@BeanParam` per iniettare i SearchRequest dalle query string
- Delegano immediatamente al Service, senza logica
- Annotano i metodi con `@Operation` e `@APIResponse` per OpenAPI

```java
@Path("/warehouses")
@ApplicationScoped
public class WarehouseResource {

    @Inject
    WarehouseService warehouseService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createWarehouse(@Valid InsertWarehouseDTO dto) {
        return Response.ok(warehouseService.createWarehouse(dto)).build();
    }
}
```

---

## Gestione Eccezioni

- Usare sempre `ServiceException` per errori di business (mai lanciare `RuntimeException` generiche)
- `ServiceException` deve sempre contenere un HTTP status code e un messaggio comprensibile per il client
- `ExceptionMapper` intercetta tutte le `ServiceException` e le converte in `ExceptionResponse` JSON

```java
throw new ServiceException(Response.Status.BAD_REQUEST,
    "Capacità magazzino insufficiente: richiesti " + volume + " m³");
```

---

## Logging

- Usare `@Slf4j` di Lombok su ogni classe che logga
- `log.info(...)` per azioni significative (creazione, aggiornamento, movimento)
- `log.error(...)` per eccezioni con `e` come ultimo parametro
- `log.debug(...)` per dettagli tecnici (query, calcoli intermedi)
- Non loggare dati sensibili

---

## Validazione

- Validare gli input **solo nei DTO di inserimento** con annotazioni Jakarta Validation
- Non duplicare la validazione nel Service per regole già coperte dai DTO
- La validazione di business (es. verifica capacità, disponibilità stock) va nel Service, non nel DTO

---

## Search Request

Ogni `XxxSearchRequest` estende `BaseSearchRequest` e:
1. Aggiunge i campi filtro specifici come query param (`@QueryParam`)
2. Implementa `filterBuilder()` che costruisce la `ExpressionList` Ebean

Il metodo `filterBuilder()` è il punto centralizzato dove si costruisce la query — non disperdere logica di filtro nel Service.

---

## ORM Ebean

- Le entità devono estendere `Model` di Ebean per avere i metodi `save()`, `delete()`, ecc.
- Per query complesse, usare il `Database` iniettato con `db.find(Entity.class).where()...`
- Le query bean generate automaticamente (prefisso `Q`) possono essere usate per type-safe queries
- Non usare SQL raw salvo per le migrazioni Flyway

---

## Configurazione

- Tutti i parametri configurabili vanno in `application.properties`
- I bean di configurazione (`@ConfigProperty`) si trovano in `config/`
- Non hardcodare valori come URL, credenziali, path di file nel codice