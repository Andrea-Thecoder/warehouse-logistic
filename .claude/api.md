# API REST

## Base URL

```
http://localhost:8090/api/v1/warehouse-logistics
```

Documentazione interattiva disponibile su:
- OpenAPI spec: `GET /docs/v1/openapi`
- Swagger UI: `GET /ui/v1/openapi`

---

## Endpoint per Risorsa

### Warehouses — `/warehouses`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `POST` | `/warehouses` | Crea un nuovo magazzino |
| `GET` | `/warehouses` | Ricerca magazzini con filtri e paginazione |

**Request POST**: `InsertWarehouseDTO`
**Response**: `SimpleResultDTO<BaseDetailWarehouseDTO>` / `PagedResultDTO<SimpleDetailWarehouseDTO>`

---

### Products — `/products`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `POST` | `/products` | Crea un nuovo prodotto |
| `GET` | `/products` | Ricerca prodotti con filtri e paginazione |
| `GET` | `/products/{id}` | Dettaglio singolo prodotto |
| `PUT` | `/products/{id}` | Aggiorna un prodotto |
| `DELETE` | `/products/{id}` | Elimina un prodotto |

**Request POST/PUT**: `InsertProductDTO`
**Response**: `SimpleResultDTO<BaseDetailProductDTO>` / `PagedResultDTO<SimpleDetailProductDTO>`

---

### Stock — `/stock`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `POST` | `/stock` | Inserisce o aggiorna una giacenza |
| `GET` | `/stock` | Ricerca giacenze con filtri e paginazione |

**Request POST**: `InsertStockDTO`
**Response**: `SimpleResultDTO<BaseDetailStockDTO>` / `PagedResultDTO<DetailStockDTO>`

---

### Movement Track — `/movement-track`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `POST` | `/movement-track` | Crea un nuovo movimento merci |
| `GET` | `/movement-track` | Ricerca movimenti con filtri e paginazione |

**Request POST**: `InsertMovementTrackDTO` (contiene `status`, `originWarehouseId`, `productId`, `quantity`, lista destinazioni)
**Response**: `SimpleResultDTO<BaseDetailMovementTrackDTO>` / `PagedResultDTO<DetailMovementTrackDTO>`

---

### Cities — `/cities`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/cities` | Ricerca città con filtri e paginazione |

---

### Regions — `/regions`

| Metodo | Path | Descrizione |
|--------|------|-------------|
| `GET` | `/regions` | Ricerca regioni |

---

### Lookup — `/lookup`

Endpoint per dati di configurazione (categorie prodotto, stati movimento, ecc.).

---

## DTO di Richiesta Comuni

### Paginazione (in tutti i SearchRequest)

```json
{
  "page": 0,
  "size": 20,
  "sort": "name",
  "descending": false
}
```

### InsertMovementTrackDTO (esempio)

```json
{
  "originWarehouseId": 1,
  "productId": 5,
  "quantity": 10,
  "status": "IN_TRANSIT",
  "note": "Spostamento urgente",
  "destinations": [
    { "warehouseId": 3, "order": 1 },
    { "warehouseId": 7, "order": 2 }
  ]
}
```

---

## DTO di Risposta

### SimpleResultDTO

```json
{
  "payload": { ... },
  "message": "Operazione completata con successo"
}
```

### PagedResultDTO

```json
{
  "records": [ ... ],
  "totalRecords": 150,
  "currentPage": 0,
  "pageSize": 20,
  "totalPages": 8
}
```

### RouteInfo (output calcolo rotta)

```json
{
  "fromCity": "Milano",
  "toCity": "Roma",
  "distanceKm": 578.3,
  "estimatedTimeMinutes": 340,
  "geometry": "LINESTRING(9.1859 45.4654, ...)"
}
```
La geometria è in formato WKT (Well-Known Text).

---

## Gestione Errori

In caso di errore, la risposta ha il seguente formato (`ExceptionResponse`):

```json
{
  "status": 400,
  "message": "Capacità magazzino insufficiente per il prodotto richiesto"
}
```

I codici HTTP seguono le convenzioni standard: `400` per dati non validi, `404` per risorse non trovate, `500` per errori interni.

---

## CORS

CORS abilitato per tutte le origini (configurazione `quarkus.http.cors=true` con regex `/.*`). Da restringere in produzione.