# Architettura

## Struttura a Layer

L'applicativo segue un'architettura a strati classica (Layered Architecture):

```
[Client HTTP]
     ↓
[API Layer]       src/main/java/.../api/          — JAX-RS Resources
     ↓
[Service Layer]   src/main/java/.../service/      — Business Logic
     ↓
[Model Layer]     src/main/java/.../model/        — Entità Ebean/DB
     ↓
[PostgreSQL]      via Ebean ORM
```

Ogni layer comunica solo con quello adiacente. I Controller non accedono al DB direttamente; i Service non espongono logica HTTP.

---

## Struttura Directory

```
src/main/java/it/warehouse/optimization/
├── api/                   Controller REST (JAX-RS Resources)
├── service/               Business logic e orchestrazione
├── model/                 Entità persistenti (Ebean)
│   └── enumerator/        Enum di dominio
├── dto/                   Data Transfer Objects
│   ├── search/            Request di ricerca con filtri e paginazione
│   ├── warehouse/         DTO magazzini
│   ├── stock/             DTO giacenze
│   ├── product/           DTO prodotti
│   ├── movementtrack/     DTO movimenti
│   └── routing/           DTO output calcolo rotte
├── config/                Bean di configurazione e inizializzazione
├── exception/             Eccezioni custom e mapping REST
└── utils/                 Utility geografiche e generiche

src/main/resources/
├── application.properties
└── dbinit/postgres/       Script Flyway (V1.0 → V1.3)
```

---

## Pattern Architetturali

### DTO Pattern
Ogni operazione CRUD ha DTO separati:
- `InsertXxxDTO` — payload della richiesta POST/PUT
- `BaseDetailXxxDTO` — campi base della risposta
- `DetailXxxDTO` / `SimpleDetailXxxDTO` — risposta completa o ridotta

### Search Request con Paginazione
Tutte le ricerche usano una gerarchia di DTO sealed:
```
BaseSearchRequest
└── XxxSearchRequest  — aggiunge filtri specifici del dominio
```
Ogni `SearchRequest` espone `filterBuilder()` per costruire la query Ebean.

### Response Envelope
Tutte le risposte sono wrappate in:
- `SimpleResultDTO<T>` — singolo oggetto + messaggio
- `PagedResultDTO<T>` — lista paginata con metadati (totalRecords, currentPage, totalPages)

### Audit Trail
Tutte le entità persistenti estendono `AbstractAuditable`, che aggiunge automaticamente:
- `_dataCreazione`, `_utenteCreazione`
- `_dataModifica`, `_utenteModifica`
- `_version` (versioning ottimistico Ebean)

### Transaction Management
Le transazioni sono gestite esplicitamente nel Service layer con Ebean:
```java
try (Transaction tx = db.beginTransaction()) {
    // operazioni
    tx.commit();
}
```
Non si usa `@Transactional` dichiarativo — la gestione è sempre esplicita.

### Exception Handling
- `ServiceException` — eccezione di dominio con codice HTTP e messaggio
- `ExceptionMapper` — mappa `ServiceException` → risposta JSON strutturata (`ExceptionResponse`)

---

## Inizializzazione all'Avvio

I bean `@Startup` eseguono all'avvio dell'applicazione:
- `GraphHopperConfig` — carica la mappa OSM in memoria (lento, ~30s)
- `MapDownloader` — scarica automaticamente `italy-latest.osm.pbf` se assente
- `ORToolsConfig` — inizializza OR-Tools

L'avvio è lento alla prima esecuzione per via del caricamento della mappa (~2GB).