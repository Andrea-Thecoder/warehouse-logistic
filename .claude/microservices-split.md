# Split in Microservizi: Logistic + Transport

## Contesto

L'applicativo `warehouse-logistic` è oggi un monolite Quarkus che gestisce due domini distinti:

- **Dominio Logistic** — magazzini, prodotti, giacenze (stock), città, regioni
- **Dominio Transport** — movimenti merci, calcolo rotte (GraphHopper, OR-Tools, JSPRIT)

Questo documento analizza la fattibilità e le modalità di split in due microservizi indipendenti.

---

## 1. Ha senso fare lo split?

### Sì — i domini sono realmente distinti

Le motivazioni tecniche sono concrete:

| Aspetto | Logistic | Transport |
|---------|----------|-----------|
| **Natura del workload** | CRUD + capacity management | Computazione pesante (VRP, routing) |
| **Startup time** | < 5s | ~30s (caricamento mappa OSM 2GB) |
| **Dipendenze Maven** | Quarkus + Ebean + Postgres | + GraphHopper + OR-Tools + JSPRIT (native libs) |
| **Scaling** | Scala uniformemente con il traffico REST | Scala in base ai calcoli di routing richiesti |
| **Deployment** | Cambia raramente (dati stabili) | Può cambiare per aggiornamenti algoritmi/mappe |
| **RAM minima** | ~256MB | ~2.5GB (mappa OSM in memoria) |

Tenere tutto insieme significa che ogni istanza del servizio carica 2GB di mappa OSM, anche se quell'istanza serve solo chiamate CRUD su magazzini.

### Il caveat — accoppiamento transazionale

Il punto critico è `MovementTrackService`, che gestisce i flussi `IN_TRANSIT`, `FROM_FACTORY`, `TO_SALE`. Oggi tutto avviene in **un'unica transazione DB**:

```
POST /movement-track (status: IN_TRANSIT)
  ├── [1] StockService.decrementStock(originWarehouse) ← dominio Logistic
  ├── [2] RoutingService.calculateRoute(...)           ← dominio Transport
  ├── [3] MovementTrackDestination.save(...)           ← dominio Transport
  └── [4] MovementStatusHistory.save(...)              ← dominio Transport
```

Passando ai microservizi, i passi [1] e [2-4] vivono su DB e processi diversi. Se il passo [2] fallisce dopo che [1] ha già eseguito, lo stock è decrementato ma il movimento non esiste — **inconsistenza dei dati**.

Questo è il problema fondamentale che le sezioni successive affrontano.

---

## 2. Cosa va dove

### `warehouse-logistic` (servizio esistente, ripulito)

**Rimane tutto ciò che riguarda magazzini, prodotti e giacenze.**

```
src/main/java/it/warehouse/optimization/
├── api/
│   ├── WarehouseResource.java
│   ├── ProductResource.java
│   ├── StockResource.java
│   ├── CityResource.java
│   ├── RegionResource.java
│   └── LookupResource.java
├── service/
│   ├── WarehouseService.java
│   ├── ProductService.java
│   ├── StockService.java
│   ├── CityService.java
│   ├── RegionService.java
│   └── LookupService.java
├── model/
│   ├── Warehouse.java
│   ├── Product.java
│   ├── Stock.java
│   ├── City.java
│   ├── Region.java
│   ├── CategoryType.java
│   └── AbstractAuditable.java
├── dto/
│   ├── warehouse/      (tutti i DTO)
│   ├── product/        (tutti i DTO)
│   ├── stock/          (tutti i DTO)
│   ├── city/           (tutti i DTO)
│   ├── region/         (tutti i DTO)
│   ├── PagedResultDTO.java
│   ├── SimpleResultDTO.java
│   └── LookupDetailDTO.java
└── config/
    ├── EbeanConfig.java
    └── CurrentUserProviderImpl.java
```

**Dipendenze Maven rimosse**: GraphHopper, OR-Tools, JSPRIT, Choco-Solver.
**Dipendenze Maven aggiunte** (solo per la soluzione con eventi): Kafka client o REST client.

**Porta**: 8090 (invariata)
**DB**: stesso schema PostgreSQL, tabelle: `warehouse`, `product`, `a_product_warehouse` (stock), `city`, `region`, `category_type`

---

### `warehouse-transport` (nuovo servizio)

**Tutto ciò che riguarda movimenti e routing.**

```
src/main/java/it/warehouse/transport/
├── api/
│   └── MovementTrackResource.java
├── service/
│   ├── MovementTrackService.java
│   ├── MovementTrackDestinationService.java
│   ├── MovementStatusHistoryService.java
│   └── RoutingService.java
├── model/
│   ├── MovementTrack.java
│   ├── MovementTrackDestination.java
│   ├── MovementStatusHistory.java
│   └── enumerator/
│       └── MovementStatus.java
├── dto/
│   ├── movementtrack/      (tutti i DTO)
│   ├── movementdestination/
│   ├── movementhistory/
│   ├── routing/
│   │   ├── SingleRouteInfo.java
│   │   └── MultiRouteInfo.java
│   ├── search/MovementSearchRequest.java
│   └── (DTO condivisi copiati o estratti in lib comune)
├── config/
│   ├── GraphHopperConfig.java   ← carica mappa OSM ~2GB
│   ├── MapDownloader.java
│   ├── ORToolsConfig.java
│   └── RoutingConfig.java
└── utils/
    └── RoutingUtils.java
```

**Porta**: 8091 (nuova)
**DB**: stesso cluster PostgreSQL, schema separato o stesso schema — tabelle: `movement_track`, `movement_track_destination`, `movement_status_history`

> **Nota su DB condiviso**: condividere il DB fisico tra due microservizi è un anti-pattern (accoppiamento implicito), ma è accettabile in fase transitoria. L'obiettivo a regime è un DB per servizio.

---

## 3. Il problema: come gestire la transazionalità distribuita

Quando Transport deve aggiornare lo stock su Logistic (dominio diverso, DB diverso), hai due approcci principali.

---

## 4. Opzione A — REST Client sincrono (approccio semplice)

Transport chiama Logistic via HTTP prima di persistere il movimento.

```
POST /movement-track (IN_TRANSIT) → [Transport]
  │
  ├── [1] HTTP POST /stock/decrement → [Logistic]  ← chiamata sincrona
  │         └── risposta: ok / errore capacità
  │
  ├── [2] RoutingService.calculateRoute(...)
  ├── [3] MovementTrack.save(...)
  └── [4] MovementStatusHistory.save(...)
```

**Implementazione in Quarkus**: MicroProfile REST Client (`@RegisterRestClient`).

```java
@RegisterRestClient(baseUri = "http://warehouse-logistic:8090")
public interface LogisticClient {
    @POST
    @Path("/api/v1/warehouse-logistics/stock/decrement")
    Response decrementStock(DecrementStockDTO dto);
}
```

### Vantaggi
- Implementazione semplice, nessuna infrastruttura aggiuntiva
- Errori sincroni e immediatamente visibili al chiamante
- Il client riceve risposta definitiva (ok/errore) nella stessa request

### Rischi

| Rischio | Descrizione | Mitigazione |
|---------|-------------|-------------|
| **Finestra di inconsistenza** | Se il passo [1] riesce ma [3] fallisce per errore di rete/crash, lo stock è decrementato senza movimento | Endpoint idempotente di compensazione (`/stock/increment`) chiamato nel catch |
| **Accoppiamento temporale** | Se Logistic è down, Transport non funziona | Circuit breaker (Quarkus Fault Tolerance) |
| **Latenza aggiuntiva** | Ogni movimento aggiunge una chiamata HTTP | Accettabile per questo dominio (non è hot path ad alta frequenza) |
| **Rollback distribuito** | Non esiste un rollback automatico cross-servizio | Compensazione manuale (vedi sotto) |

**Pattern di compensazione manuale**:
```java
try {
    logisticClient.decrementStock(dto);  // [1]
    movementTrack.save();                // [2]
} catch (Exception e) {
    logisticClient.incrementStock(dto);  // compensazione
    throw e;
}
```
Funziona nella maggior parte dei casi, ma non è bulletproof: se la compensazione stessa fallisce, serve un job di riconciliazione periodica.

**Quando sceglierla**: team piccolo, requisiti di consistenza non critici, bassa frequenza di movimenti, si vuole evitare infrastruttura Kafka.

---

## 5. Opzione B — Saga Pattern con Kafka (approccio robusto)

I due servizi comunicano tramite eventi asincroni. Ogni step del flusso pubblica un evento; se qualcosa va storto, si pubblica un evento di compensazione.

### Flusso normale (Choreography-based Saga)

```
Client → POST /movement-track → [Transport]
           │
           └── pubblica: MovementInitiated { movementId, warehouseId, quantity, product }
                                │
                    [Logistic] consuma MovementInitiated
                           │
                           ├── decrementa stock
                           └── pubblica: StockDecremented { movementId, success: true }
                                              │
                                  [Transport] consuma StockDecremented
                                         │
                                         ├── calcola rotta
                                         ├── salva MovementTrack (status: IN_TRANSIT)
                                         └── pubblica: MovementConfirmed { movementId }
```

### Flusso di compensazione (rollback)

```
[Transport] calcola rotta → fallisce
      │
      └── pubblica: MovementFailed { movementId }
                         │
             [Logistic] consuma MovementFailed
                    │
                    └── ripristina stock (incremento)
                    └── pubblica: StockRestored { movementId }
```

### Topici Kafka suggeriti

| Topico | Publisher | Consumer | Payload |
|--------|-----------|----------|---------|
| `movement.initiated` | Transport | Logistic | movementId, warehouseId, quantity, productId |
| `stock.decremented` | Logistic | Transport | movementId, success, availableQuantity |
| `movement.confirmed` | Transport | (audit/notify) | movementId, routeInfo |
| `movement.failed` | Transport | Logistic | movementId, reason |
| `stock.restored` | Logistic | (audit) | movementId |

### Implementazione in Quarkus (Reactive Messaging)

```java
// Transport — pubblica evento
@Inject
@Channel("movement-initiated")
Emitter<MovementInitiated> emitter;

public void createMovement(InsertMovementTrackDTO dto) {
    String movementId = UUID.randomUUID().toString();
    emitter.send(new MovementInitiated(movementId, dto));
    // risposta al client: 202 Accepted + movementId
}

// Logistic — consuma evento
@Incoming("movement-initiated")
@Outgoing("stock-decremented")
public StockDecremented handleMovementInitiated(MovementInitiated event) {
    boolean success = stockService.decrement(event.warehouseId(), event.quantity());
    return new StockDecremented(event.movementId(), success);
}
```

### Vantaggi

| Vantaggio | Dettaglio |
|-----------|-----------|
| **Disaccoppiamento totale** | Logistic e Transport non si conoscono — comunicano solo tramite eventi |
| **Resilienza** | Se Logistic è temporaneamente down, gli eventi si accumulano nel topic Kafka e vengono processati appena torna su |
| **Audit trail nativo** | Ogni evento è persistito in Kafka — hai la storia completa di ogni transazione distribuita |
| **Scalabilità indipendente** | Puoi aumentare le partizioni Kafka e i consumer separatamente per ciascun servizio |
| **Rollback strutturato** | Il rollback è un evento come gli altri, non codice di compensazione ad hoc |

### Rischi

| Rischio | Descrizione | Mitigazione |
|---------|-------------|-------------|
| **Consistenza eventuale** | Il client riceve 202 Accepted: il movimento non è confermato istantaneamente | Polling su `GET /movement-track/{id}` o WebSocket per notifica finale |
| **Complessità operativa** | Kafka cluster da gestire (broker, zookeeper/KRaft, topic, schema registry) | Confluent Cloud / RedPanda come managed service |
| **Idempotenza obbligatoria** | Gli eventi possono essere ricevuti più volte (at-least-once delivery) | Consumer idempotenti: controllo `movementId` già processato prima di agire |
| **Saga state tracking** | In caso di errore parziale, occorre sapere a che punto si è fermata la saga | Tabella `saga_state` su DB Transport con stato corrente per ogni `movementId` |
| **Debug più difficile** | Il flusso è distribuito su più eventi/log | Distributed tracing (OpenTelemetry + Jaeger) |
| **Dead letter queue** | Se la compensazione stessa fallisce, il messaggio finisce in DLQ — serve monitoring | DLQ + alert + job di riconciliazione manuale |

---

## 6. Confronto diretto

| Criterio | REST sincrono | Saga + Kafka |
|----------|---------------|--------------|
| **Complessità implementativa** | Bassa | Alta |
| **Infrastruttura aggiuntiva** | Nessuna | Kafka cluster |
| **Consistenza** | Forte (con rischio finestra) | Eventuale |
| **Resilienza a failure** | Bassa (dipendenza sincrona) | Alta |
| **UX client** | Risposta immediata (200/400) | Risposta differita (202 + polling) |
| **Auditability** | Solo log applicativi | Event log nativo su Kafka |
| **Adatto a** | Piccoli team, bassa frequenza | Team strutturati, alta frequenza |
| **Rollback** | Manuale, fragile | Strutturato tramite eventi |
| **Observability** | Standard (log/metrics) | Richiede distributed tracing |

---

## 7. Raccomandazione

### Per un progetto di crescita graduale

Implementa lo split in due fasi:

**Fase 1 — Split con REST Client sincrono**
- Separa i due servizi fisicamente (due Quarkus app, due Dockerfile)
- Transport chiama Logistic via MicroProfile REST Client
- Aggiungi compensazione manuale nel catch
- Aggiungi Circuit Breaker con `@CircuitBreaker` (Quarkus Fault Tolerance)
- Mantieni DB condiviso temporaneamente

Questo ti dà subito i benefici del deployment indipendente e dell'isolamento delle dipendenze pesanti (GraphHopper, OR-Tools), con complessità minima.

**Fase 2 — Migrazione a Kafka (quando serve)**
- Introduce Kafka quando la frequenza dei movimenti giustifica la resilienza asincrona
- Migra topic per topic, mantenendo backward compatibility
- Separa i DB fisicamente in questa fase

### Segnali che indicano che è ora di passare a Kafka
- Il servizio Logistic va down spesso e blocca Transport
- Il volume di movimenti supera ~100/minuto
- Serve un audit trail affidabile per compliance
- Il team è abbastanza grande da gestire la complessità operativa

---

## 8. Struttura Docker Compose aggiornata

```yaml
services:

  warehouse-logistic:
    build: ./warehouse-logistic
    ports:
      - "8090:8090"
    environment:
      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/warehouse
    depends_on:
      - postgres

  warehouse-transport:
    build: ./warehouse-transport
    ports:
      - "8091:8091"
    environment:
      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/warehouse
      - LOGISTIC_SERVICE_URL=http://warehouse-logistic:8090   # solo per Opzione A
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092                    # solo per Opzione B
    depends_on:
      - postgres
      - warehouse-logistic   # solo per Opzione A

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: warehouse
      POSTGRES_USER: warehouse
      POSTGRES_PASSWORD: warehouse

  # Solo per Opzione B
  kafka:
    image: apache/kafka:3.7.0
    ports:
      - "9092:9092"
```

---

## 9. Checklist per lo split

- [ ] Creare repository `warehouse-transport` (o modulo Maven separato)
- [ ] Spostare classi Transport nel nuovo progetto
- [ ] Rimuovere dipendenze GraphHopper/OR-Tools/JSPRIT da `warehouse-logistic`
- [ ] Scegliere Opzione A o B per la comunicazione cross-servizio
- [ ] Implementare comunicazione + compensazione/saga
- [ ] Aggiornare Docker Compose
- [ ] Aggiornare Flyway migrations (decidere se DB condiviso o separato)
- [ ] Aggiornare documentazione API (due Swagger UI distinti)
- [ ] Verificare che i DTO condivisi siano copiati o estratti in libreria comune
- [ ] Aggiungere health check (`/q/health`) a entrambi i servizi
- [ ] Configurare distributed tracing (OpenTelemetry) se si sceglie Kafka