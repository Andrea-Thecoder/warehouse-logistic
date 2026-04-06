# Warehouse Logistic — Guida al Progetto

## Descrizione dell'Applicativo

**Warehouse Logistic** è un'API REST per la gestione ottimizzata della logistica di magazzino su territorio italiano.

Il sistema permette di:
- Censire **magazzini** con capacità di volume e peso, associati a città geografiche
- Gestire il **catalogo prodotti** con dimensioni fisiche (volume/peso)
- Tracciare le **giacenze** (stock) di prodotti per ogni magazzino
- Registrare i **movimenti di merci** tra magazzini, da fabbrica, verso punti vendita
- Calcolare **rotte ottimizzate** tra magazzini usando mappe OpenStreetMap reali e algoritmi VRP

L'applicativo usa le mappe stradali italiane reali (OpenStreetMap ~2GB) per calcolare distanze e tempi di percorrenza effettivi. Per percorsi con più tappe, applica un algoritmo di Vehicle Routing Problem con OR-Tools (matrice Haversine per ottimizzazione + GraphHopper per geometria reale).

**Stack principale**: Quarkus 3 + Ebean ORM + PostgreSQL + GraphHopper + OR-Tools
**Porta**: 8090
**Base path**: `/api/v1/warehouse-logistics`

---

## Sezioni della Documentazione

| Sezione | File | Contenuto |
|---------|------|-----------|
| Architettura | [architecture.md](.claude/architecture.md) | Struttura a layer, pattern architetturali, struttura directory |
| Stack Tecnologico | [tech-stack.md](.claude/tech-stack.md) | Dipendenze Maven, framework, motori di routing |
| Modello Dati | [data-model.md](.claude/data-model.md) | Entità, relazioni, schema DB, migrazioni Flyway |
| API REST | [api.md](.claude/api.md) | Endpoint, request/response DTO, paginazione |
| Business Logic | [business-logic.md](.claude/business-logic.md) | Flussi principali, stati dei movimenti, calcolo rotte |
| Convenzioni | [conventions.md](.claude/conventions.md) | Naming, pattern di codice, gestione eccezioni, logging |
| Algoritmo Haversine | [haversine-routing.md](.claude/haversine-routing.md) | Teoria e pratica della matrice Haversine per l'ottimizzazione multi-tappa |