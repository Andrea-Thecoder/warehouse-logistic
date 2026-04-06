**Warehouse Logistic** è un applicativo che gestisce lo stoccaggio dei prodotti nel magazzino e calcola la rotta più efficiente per spostare merci tra due o più magazzini.

**Versione attuale**: **0.1.0**


## **Avviare il progetto**

### **Avviare Docker**

Per avviare Docker, posizionarsi con un terminale nella cartella `docker` ed eseguire il comando: `docker compose up -d`

Controllare che tutti i container (2) siano avviati correttamente.

## **Avviare Quarkus**

Per avviare l'applicativo, posizionarsi nella cartella del progetto **warehouse-logistic** ed eseguire: `./mvnw quarkus:dev`

### **Caricamento dei Dati**

**Warehouse Logistic** richiede un file **osm.pbf** che rappresenta la mappa della nazione in cui si vogliono calcolare le rotte di trasporto logistico.

- Il file può essere scaricato **automaticamente** al primo avvio se l’impostazione `warehouse.map.auto-download` in `application.properties` è impostata a `true`.
- In alternativa, è possibile eseguire il download **manualmente** tramite lo script Python.

**Nota bene**: Il file pesa circa 2 GB.

#### **Download osm.pbf manuale

Posizionarsi nella cartella **python** ed eseguire: `python3 gh-map-downloader.py`  ed attendere che il download sia completato.

**Nota**: Richiedete python installato globalmente.

#### **Parametrizzazione dei valori**
I valori utilizzati per il caricamento delle mappe sono **parametrizzabili**.

Quando il download è **automatico**, questi valori devono essere impostati in `application.properties`:
```
warehouse.map.auto-download=<true|false>
warehouse.map.connection-timeout=<TIMEOUT_IN_MINUTES>
warehouse.map.file-path=<OUTPUT_DIRECTORY>
warehouse.map.file-name=<FILENAME.osm.pbf>
warehouse.map.country=<COUNTRY>
warehouse.map.continent=<CONTINENT>
```
Quando il download è **manuale** tramite lo script Python, gli stessi valori possono essere passati come argomenti:
```
python3 gh-map-downloader.py \
    --continent "<CONTINENT>" \
    --country "<COUNTRY>" \
    --directory "<OUTPUT_DIRECTORY>" \
    --filename "<FILENAME>" \
    --timeout <TIMEOUT_IN_MINUTES>
```

### Valori Parametrizzati

Oltre ai valori per il caricamento delle mappe, è possibile modificare altri parametri dell’applicativo in `application.properties` usando valori generici da sostituire:
```
warehouse.coordinates.validation.enabled=<true|false> 
warehouse.lat.min=<LAT_MIN> 
warehouse.lat.max=<LAT_MAX> 
warehouse.lon.min=<LON_MIN> 
warehouse.lon.max=<LON_MAX> 
warehouse.routing.language=<LANGUAGE_CODE>
```

