# Stack Tecnologico

## Runtime e Framework

| Tecnologia | Versione | Ruolo |
|-----------|----------|-------|
| Java | 21 | Linguaggio principale |
| Quarkus | 3.25.2 | Framework applicativo (alternativa leggera a Spring Boot) |
| Jakarta REST (JAX-RS) | — | Standard REST API |
| Jakarta CDI | — | Dependency Injection (`@ApplicationScoped`, `@Inject`) |
| Jakarta Validation | — | Validazione DTO (`@NotNull`, `@Min`, `@Valid`) |
| Lombok | 1.18.38 | Riduzione boilerplate (`@Getter`, `@Slf4j`, `@Builder`, ecc.) |

---

## Persistenza

| Tecnologia | Versione | Ruolo |
|-----------|----------|-------|
| Ebean ORM | 15.3.0 | ORM alternativo a Hibernate, più lightweight |
| PostgreSQL | 17.2 | Database relazionale |
| Flyway | — | Database migration (integrato in Quarkus) |

Ebean usa le proprie annotazioni ma è compatibile con JPA. Le query vengono costruite tramite `ExpressionList` e i query bean generati automaticamente (plugin `querybean-generator`).

---

## Routing e Ottimizzazione

| Tecnologia | Versione | Ruolo |
|-----------|----------|-------|
| GraphHopper | 10.2 | Motore di routing su mappa OpenStreetMap reale |
| JSPRIT | 1.9.0-beta.12 | Vehicle Routing Problem (VRP) solver multi-tappa |
| OR-Tools (Google) | 9.6.2534 | Constraint solving (incluso, in fase di integrazione) |
| Choco-Solver | 4.10.8 | Constraint programming alternativo |

**GraphHopper** carica la mappa `italy-latest.osm.pbf` (~2GB) in una cache locale (`graph-cache/`) e risponde a query di routing punto-a-punto con distanza in metri e tempo in millisecondi.

**JSPRIT** implementa l'algoritmo di ottimizzazione VRP: dato un magazzino di partenza e N destinazioni, trova l'ordine di visita che minimizza la distanza/tempo totale.

---

## API e Documentazione

| Tecnologia | Ruolo |
|-----------|-------|
| SmallRye OpenAPI | Genera spec OpenAPI automaticamente dalle annotazioni |
| Swagger UI | Interfaccia grafica per testare le API (`/ui/v1/openapi`) |

---

## Utility e Logging

| Tecnologia | Ruolo |
|-----------|-------|
| SLF4J + JBoss Logging | Logging (tramite Lombok `@Slf4j`) |
| Apache Commons Lang 3 | Utility stringhe e array |
| Apache Commons Collections 4 | Strutture dati avanzate |
| Apache Commons IO 2.16.1 | Operazioni file system |
| Jackson | Serializzazione/deserializzazione JSON |

---

## Testing e Qualità

| Tecnologia | Ruolo |
|-----------|-------|
| JUnit 5 | Unit e integration test |
| REST Assured | Test di API REST |
| Mockito 5 | Mocking nei test unitari |
| JaCoCo | Code coverage |
| SonarQube | Analisi statica della qualità del codice |

---

## Infrastruttura

| Tecnologia | Ruolo |
|-----------|-------|
| Docker / Docker Compose | Esecuzione PostgreSQL in container locale |
| GraalVM | Supporto native image (non attivo di default) |

### Docker Compose
Il file `docker-compose/docker-compose.yaml` avvia:
- PostgreSQL sulla porta **5433** (non 5432 per evitare conflitti con istanze locali)
- Database: `warehouse_optimization`, schema: `dev`
- Credenziali: `warehouse` / `optimization`

---

## Mappa OpenStreetMap

Il file `italy-latest.osm.pbf` viene scaricato automaticamente dal server Geofabrik se non presente in `data/`.
La prima elaborazione costruisce la cache in `graph-cache/` (operazione lenta, ~1-2 minuti).
Nelle esecuzioni successive, la cache viene caricata direttamente (~30 secondi).