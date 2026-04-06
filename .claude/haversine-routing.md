# Haversine e Matrice delle Distanze

## Il problema di partenza

Per ottimizzare un percorso con N tappe (TSP — Travelling Salesman Problem), l'algoritmo di OR-Tools ha bisogno di sapere **quanto costa andare da ogni punto a ogni altro punto**. Questo "costo" è la distanza.

Il modo più preciso sarebbe interrogare GraphHopper per ogni coppia possibile, ma con N destinazioni le interrogazioni sarebbero **N²** (con 10 magazzini: 100 chiamate). Costoso.

La soluzione è usare la **formula di Haversine** per calcolare le distanze direttamente dalle coordinate geografiche, in puro Java, senza chiamate esterne.

---

## La formula geodetica di Haversine

### Il problema geometrico

La Terra non è piatta. Due punti con coordinate (lat₁, lon₁) e (lat₂, lon₂) non sono separati dalla distanza euclidea `√(Δx² + Δy²)` — quella formula vale su un piano, non su una sfera.

La distanza reale tra due punti sulla superficie terrestre si chiama **distanza ortodromica** (o *great-circle distance*): è l'arco del cerchio massimo che passa per i due punti.

### La formula

Haversine è una formula trigonometrica che calcola questa distanza con alta precisione. Prende il nome dalla funzione `hav(θ) = sin²(θ/2)`.

Dati due punti P₁ = (φ₁, λ₁) e P₂ = (φ₂, λ₂) in radianti:

```
Δφ = φ₂ − φ₁
Δλ = λ₂ − λ₁

a = sin²(Δφ/2) + cos(φ₁) · cos(φ₂) · sin²(Δλ/2)

c = 2 · atan2(√a, √(1−a))

d = R · c
```

Dove:
- `φ` è la latitudine, `λ` è la longitudine (entrambi in **radianti**)
- `R` = 6 371 000 m (raggio medio della Terra)
- `d` è la distanza in metri

### Cosa rappresenta ogni parte

| Variabile | Significato |
|-----------|-------------|
| `Δφ` | Differenza di latitudine tra i due punti |
| `Δλ` | Differenza di longitudine tra i due punti |
| `a` | Quadrato del seno del semi-angolo centrale (su entrambe le direzioni) |
| `c` | Angolo centrale in radianti sotteso dai due punti visto dal centro della Terra |
| `d` | Lunghezza dell'arco = raggio × angolo centrale |

La funzione `atan2(√a, √(1−a))` è numericamente più stabile di `asin(√a)` per angoli piccoli o grandi.

### Implementazione nel progetto

```java
// RoutingService.java — metodo haversineMeters()
private long haversineMeters(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6_371_000.0;                       // raggio Terra in metri
    double dLat = Math.toRadians(lat2 - lat1);          // Δφ in radianti
    double dLon = Math.toRadians(lon2 - lon1);          // Δλ in radianti
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) // sin²(Δφ/2)
             + Math.cos(Math.toRadians(lat1))
             * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon / 2) * Math.sin(dLon / 2); // + cos·cos·sin²(Δλ/2)
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); // angolo centrale
    return Math.round(R * c);                           // distanza in metri (intero)
}
```

---

## La matrice di Haversine

### Cos'è

Una matrice di Haversine è una **matrice quadrata N×N** in cui la cella `[i][j]` contiene la distanza geodetica (in metri) tra il punto `i` e il punto `j`.

Con N = 4 punti (origine + 3 magazzini):

```
         Origine   Mag.A   Mag.B   Mag.C
Origine [    0    |  312km | 185km | 540km ]
Mag.A   [  312km |    0   | 203km | 410km ]
Mag.B   [  185km | 203km  |   0   | 280km ]
Mag.C   [  540km | 410km  | 280km |    0  ]
```

La diagonale è sempre 0 (distanza di un punto da se stesso). La matrice è **simmetrica** (distanza A→B = distanza B→A su sfera).

### Costruzione nel progetto

```java
// RoutingService.java — metodo buildHaversineMatrix()
private long[][] buildHaversineMatrix(double[] lats, double[] lons, int size) {
    long[][] matrix = new long[size][size];
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            matrix[i][j] = haversineMeters(lats[i], lons[i], lats[j], lons[j]);
        }
    }
    return matrix;
}
```

L'array `lats`/`lons` ha questa convenzione:
- Indice `0` → città di origine
- Indice `1..n` → città dei magazzini destinazione

Il costo è **O(N²)** in tempo di calcolo, ma sono solo operazioni matematiche in memoria — con 20 magazzini sono 400 moltiplicazioni, eseguito in microsecondi.

---

## Come viene usata nel percorso multi-tappa

### Flusso completo

```
Coordinate (lats[], lons[])
        ↓
buildHaversineMatrix()          ← O(N²) math pura, ~microsecondi
        ↓
OR-Tools TSP (solveWithOrTools) ← legge dalla matrice per trovare ordine ottimale
        ↓
Ordine ottimale [2, 0, 1]       ← "visita prima Mag.C, poi Mag.A, poi Mag.B"
        ↓
GraphHopper (N chiamate)        ← una per ogni tratta nell'ordine trovato
        ↓
Lista RouteInfo con geometria WKT + istruzioni turn-by-turn
```

### Ruolo di Haversine vs GraphHopper

| | Haversine | GraphHopper |
|---|---|---|
| **Scopo** | Decidere l'ordine di visita | Calcolare il percorso reale |
| **Distanza** | Geodetica (linea d'aria) | Stradale (strade reali) |
| **Velocità** | Microsecondi | ~50-200ms per tratta |
| **Precisione** | Alta per confronti relativi | Esatta |
| **Chiamate** | 0 (pure math) | N (una per tratta) |

### Perché Haversine è sufficiente per l'ottimizzazione

La distanza geodetica (linea d'aria) e la distanza stradale sono **proporzionalmente correlate**: se Milano è più vicina a Torino che a Napoli sulla linea d'aria, lo è quasi certamente anche su strada.

L'errore tipico è del 15-30% in valore assoluto (Haversine ≤ distanza stradale), ma per decidere **quale ordine di visita è migliore** conta solo il confronto relativo tra percorsi alternativi — e lì Haversine è affidabile.

L'alternativa sarebbe una matrice N×N di distanze stradali reali (via GraphHopper), che richiederebbe N² chiamate. Con 10 magazzini: 100 chiamate invece di 10. Non vale il costo.

---

## Limiti e casi limite

| Situazione | Comportamento |
|-----------|---------------|
| Due punti con le stesse coordinate | Distanza = 0, corretto |
| Punto antipodale (dall'altra parte della Terra) | Formula stabile grazie ad `atan2` |
| Distanze brevi (< 1 km) | Errore trascurabile (< 0.01%) |
| Reti stradali tortuose (montagna, isole) | Haversine sottostima la distanza reale, ma l'ordine rimane generalmente corretto |
| Magazzino raggiungibile solo via traghetto | Haversine non lo sa — GraphHopper lo scopre nella fase di routing reale |