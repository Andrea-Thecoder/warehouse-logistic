# Modello Dati

## Schema Entità-Relazioni

```
Region (1) ──── (*) City
                     │
                     └──── (1) Warehouse ──── (*) Stock ──── (1) Product
                                  │
                                  └──── (*) MovementTrack ──── (1) Product
                                               │
                                               ├──── (*) MovementTrackDestination ──── (1) Warehouse
                                               └──── (*) MovementStatusHistory
```

---

## Entità Principali

### Region
Tabella: `region`
Dati geografici delle 20 regioni italiane. Popolata da migrazione `V1.2`.

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| name | String | Nome regione |

### City
Tabella: `city`
~8000 città italiane con coordinate geografiche. Popolata da migrazione `V1.2`.

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| name | String | Nome città |
| latitude | Double | Latitudine WGS84 |
| longitude | Double | Longitudine WGS84 |
| region | Region | FK → region |

### Warehouse
Tabella: `warehouse`
Magazzino fisico con capacità di stoccaggio.

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| name | String | Nome magazzino |
| city | City | FK → city |
| totalVolume | Double | Capacità totale (m³) |
| availableVolume | Double | Capacità attualmente disponibile (m³) |
| totalWeight | Double | Capacità totale (kg) |
| availableWeight | Double | Capacità attualmente disponibile (kg) |

### Product
Tabella: `product`

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| name | String | Nome prodotto |
| description | String | Descrizione |
| volume | Double | Volume unitario (m³) |
| weight | Double | Peso unitario (kg) |
| category | CategoryType | FK → category_type |

### Stock
Tabella: `a_product_warehouse` (tabella associativa con dati extra)
Giacenza di un prodotto in un magazzino.

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| warehouse | Warehouse | FK → warehouse |
| product | Product | FK → product |
| quantity | Integer | Quantità disponibile |
| reservedQuantity | Integer | Quantità riservata (non disponibile) |

### MovementTrack
Tabella: `movement_track`
Movimento di merci — può essere tra magazzini, da fabbrica o verso punto vendita.

| Campo | Tipo | Note |
|-------|------|------|
| id | UUID | PK |
| originWarehouse | Warehouse | FK → warehouse (sorgente), nullable |
| product | Product | FK → product |
| quantity | Integer | Quantità mossa |
| status | MovementStatus | Stato corrente (enum), aggiornato ad ogni transizione |
| estimatedTotalDistanceMeters | BigDecimal | Distanza totale stimata (metri) |
| estimatedTotalDurationMillis | BigDecimal | Durata totale stimata (ms) |
| estimatedFinalDateForFinalTravel | LocalDateTime | Data/ora stimata di arrivo finale |

### MovementTrackDestination
Tabella: `a_movement_track_destination`
Tappe di consegna associate a un movimento (supporto multi-tappa).

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| movementTrack | MovementTrack | FK → movementtrack |
| destinationWarehouse | Warehouse | FK → warehouse (destinazione) |
| order | Integer | Ordine della tappa |

### MovementStatusHistory
Tabella: `movement_status_history`
Cronologia degli stati attraversati da un movimento.

| Campo | Tipo | Note |
|-------|------|------|
| id | Long | PK |
| movementTrack | MovementTrack | FK → movementtrack |
| status | MovementStatus | Stato registrato |
| timestamp | Date | Momento del cambio stato |
| note | String | Note opzionali |

### CategoryType (lookup)
Tabella: `category_type`
Categorie di prodotto. Estende `BasicType`.

Valori precaricati: `ELECTRONICS`, `FOOD_BEVERAGES`, `FURNITURE`, `TOYS`, `TOOLS_HARDWARE`

---

## Enumerazioni di Dominio

### MovementStatus
Stati di un `MovementTrack`:

| Valore | Significato |
|--------|-------------|
| `SENT` | Merce inviata |
| `RECEIVED` | Merce ricevuta a destinazione |
| `IN_TRANSIT` | In transito tra magazzini |
| `CANCELLED` | Movimento annullato |
| `FROM_FACTORY` | Ingresso merce da fabbrica esterna |
| `TO_SALE` | Uscita verso punto vendita |

### StockAction
Usato da `StockService` per aggiornare la capacità del magazzino:

| Valore | Effetto |
|--------|---------|
| `INCREASE` | Aggiunge capacità disponibile |
| `DECREASE` | Sottrae capacità disponibile |

---

## Audit Automatico

Ogni entità estende `AbstractAuditable`. Ebean popola automaticamente:

```java
@WhenCreated  Date _dataCreazione;
@WhoCreated   String _utenteCreazione;
@WhenModified Date _dataModifica;
@WhoModified  String _utenteModifica;
@Version      Long _version;    // ottimistic locking
```

---

## Migrazioni Flyway

Le migrazioni si trovano in `src/main/resources/dbinit/postgres/` e vengono eseguite automaticamente all'avvio.

| File | Contenuto |
|------|-----------|
| `V1.0__schema.sql` | Creazione di tutte le tabelle, FK, indici, check constraints |
| `V1.1__config.sql` | Dati di configurazione iniziale |
| `V1.2__region-city.sql` | 20 regioni + ~8000 città italiane con coordinate |
| `V1.3__lookup.sql` | 5 categorie prodotto (`CategoryType`) |

**Configurazione Flyway** in `application.properties`:
```properties
quarkus.flyway.enabled=true
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=dbinit/postgres
```