# Business Logic

## Flussi Principali

---

### 1. Creazione Stock

Inserisce o aggiorna la giacenza di un prodotto in un magazzino.

```
POST /stock  (InsertStockDTO)
  ↓
StockService.createStock()
  ├── Recupera Warehouse e Product (verifica esistenza)
  ├── Controlla capacità disponibile del magazzino
  │     ├── volume richiesto = product.volume × quantity
  │     └── peso richiesto = product.weight × quantity
  ├── Se Stock esiste già → incrementa quantity
  ├── Se Stock non esiste → crea nuovo record
  └── Aggiorna availableVolume e availableWeight su Warehouse
```

La capacità del magazzino viene decrementata ad ogni aggiunta di stock e ripristinata ad ogni rimozione.

---

### 2. Movimento Merci

Il flusso varia in base al campo `status` nella richiesta.

#### IN_TRANSIT (tra magazzini)
```
POST /movement-track (status=IN_TRANSIT)
  ↓
MovementTrackService.handleStatusInTransit()
  ├── Verifica originWarehouse esiste
  ├── Verifica product e stock disponibile
  ├── StockService.decrementStock() — riduce stock magazzino origine
  ├── Crea MovementTrack con lista destinations
  ├── Crea MovementStatusHistory (stato iniziale)
  └── Commit transazione
```
Lo stock a destinazione viene incrementato solo quando si riceve un `RECEIVED`.

#### FROM_FACTORY (ingresso da fabbrica)
```
MovementTrackService.handleStatusFromFactory()
  ├── Non richiede magazzino di origine
  ├── Crea MovementTrack
  ├── StockService.increaseStock() — aggiunge stock al magazzino destinazione
  └── Commit transazione
```

#### TO_SALE (uscita verso vendita)
```
MovementTrackService.handleStatusToSale()
  ├── Verifica stock disponibile in originWarehouse
  ├── StockService.decrementStock()
  ├── Crea MovementTrack (nessuna destinazione warehouse)
  └── Commit transazione
```

---

### 3. Calcolo Rotta Singola

```
RoutingService.calculateRoute(originCity, destinationCity)
  ├── Valida coordinate (entro bounding box Italia)
  ├── Invia richiesta a GraphHopper (mappa OSM reale)
  ├── Riceve distanza (metri) e tempo (ms)
  └── Restituisce RouteInfo con:
        ├── distanceKm
        ├── estimatedTimeMinutes
        └── geometry (WKT LineString)
```

---

### 4. Calcolo Rotta Multi-Tappa (VRP)

```
RoutingService.calculateMultiDropRoute(originCity, warehouseSet)
  ├── 1. Crea un Vehicle JSPRIT con punto di partenza = originCity
  ├── 2. Per ogni warehouse destinazione → crea un Service JSPRIT
  ├── 3. Costruisce VehicleRoutingProblem
  ├── 4. Lancia l'algoritmo JSPRIT (ottimizzazione VRP)
  ├── 5. Per ogni attività nella soluzione ottimale:
  │       ├── Calcola distanza/tempo da posizione corrente a prossima
  │       │     via GraphHopper
  │       ├── Crea RouteInfo con geometria
  │       └── Avanza posizione corrente
  └── 6. Restituisce lista ordinata di RouteInfo + summary totale
```

L'algoritmo minimizza la distanza/tempo totale del percorso. Il tempo stimato usa una media di **9 ore di guida al giorno** per calcolare giorni necessari.

---

## Regole di Business

### Capacità Magazzino
- Un magazzino ha `totalVolume` e `totalWeight` fisse
- `availableVolume` e `availableWeight` vengono aggiornate ad ogni operazione di stock
- Se la capacità disponibile non è sufficiente per un'operazione, viene lanciata `ServiceException` con HTTP 400

### Disponibilità Stock
- Prima di decrementare, si verifica `stock.quantity >= quantityRichiesta`
- Se la quantità non è disponibile → `ServiceException` HTTP 400
- `reservedQuantity` è prevista nel modello ma la logica di prenotazione non è ancora implementata

### Validazione Coordinate
La validazione geografica è configurabile (`warehouse.coordinates.validation.enabled`).
Se abilitata, le coordinate devono rientrare nel bounding box Italia:
- Latitudine: 35.0 – 48.5
- Longitudine: 6.0 – 19.0

### Stato Movimento
Gli stati del movimento non seguono una macchina a stati rigida: ogni POST a `/movement-track` crea un nuovo `MovementTrack` con lo stato indicato. La `MovementStatusHistory` registra tutti gli stati nel tempo.

---

## Gestione Transazioni

Tutte le operazioni che modificano più entità (es. decrementa stock + crea movimento + aggiorna capacità magazzino) avvengono in un'unica transazione Ebean esplicita. In caso di errore, il rollback è automatico.

Il metodo `updateWarehouseCapacityNoTransaction()` esiste per essere invocato dall'interno di una transazione già aperta.