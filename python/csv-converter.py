import csv
from collections import defaultdict
from enum import Enum

class Column(Enum):
    ID = "id"
    NAME = "name"
    REGION_ID = "region_id"
    ISTAT_CODE = "istat_code"
    PROVINCE = "province"
    LATITUDE = "latitude"
    LONGITUDE = "longitude"



REGION_FILE_NAME = "regioni.csv"
CITY_FILE_NAME = "comuni.csv"
PROVINCE_FILE_NAME = 'province.csv'
SQL_FILE_NAME = "V1.2__region-city.sql"



REGION_COLUMNS =  [Column.ID,Column.NAME,Column.ISTAT_CODE]
CITIES_COLUMNS = [Column.NAME,Column.PROVINCE,Column.ISTAT_CODE,Column.REGION_ID,Column.LATITUDE,Column.LONGITUDE]


region_id_map = {}
region_to_provinces = defaultdict(list)
sql_query = ""


def comma_setting(i,length) ->str:
    if i == length - 1:
        return ";\n\n"  
    else:
        return ",\n"

def insert_create(name:str , columns) -> str:
    columnsValues = [col.value for col in columns]
    sql_query= f"INSERT INTO dev.{name} ("
    sql_query += ", ".join(columnsValues)
    sql_query += f") VALUES \n"
    return sql_query

def escape_sql(value: str) -> str:
    """
    Escapes single quotes in string values for SQL.
    Se il valore è vuoto o None, ritorna NULL.
    Altrimenti lo avvolge tra apici e sostituisce ' con ''.
    """
    if value is None or value.strip() == "":
        return "NULL"
    return "'" + value.strip().replace("'", "''") + "'"

def region_id_from_province(province) -> int :
    if not province or province.isspace(): return None 

    for istat_code, province_list in region_to_provinces.items():
        if province in province_list :
            return region_id_map.get(istat_code)
        
    

with open(REGION_FILE_NAME, newline='', encoding='utf-8') as region_file:
    region_list = list(csv.DictReader(region_file, delimiter=';'))
    region_id = 1
    sql_query = insert_create("region",REGION_COLUMNS)

    for i,row in enumerate(region_list):
        name = escape_sql(row["denominazione_regione"])
        istat_code = escape_sql(row["codice_regione"])
        sql_query += f"({region_id}, {name}, {istat_code})" + comma_setting(i, len(region_list)) 
        region_id_map[istat_code] = region_id
        region_id +=1


with open(PROVINCE_FILE_NAME, newline='', encoding='utf-8') as province_file:
    province_list = list(csv.DictReader(province_file, delimiter=';'))

    for i,row in enumerate(province_list):
        codice_regione = escape_sql(row["codice_regione"])
        sigla_provincia = escape_sql(row["sigla_provincia"])

        if sigla_provincia not in region_to_provinces[codice_regione]:
            region_to_provinces[codice_regione].append(sigla_provincia)

with open(CITY_FILE_NAME, newline='', encoding='utf-8') as city_file:
    city_list = list(csv.DictReader(city_file, delimiter=';'))
    sql_query += insert_create("city",CITIES_COLUMNS)

    for i,row in enumerate(city_list):
        name = escape_sql(row["denominazione_ita"])
        province = escape_sql(row["sigla_provincia"])
        istat_code = escape_sql(row["codice_istat"])
        latitude = float(row["lat"].replace(",","."))
        longitude = float(row["lon"].replace(",","."))
        region_id = region_id_from_province(province)


        sql_query +=f"({name}, {province}, {istat_code},{region_id}, {latitude}, {longitude})" +comma_setting(i, len(city_list))


with open(SQL_FILE_NAME, "w", encoding="utf-8") as sql_file:
    sql_file.write(sql_query)
    print("Script SQL generato correttamente. Nome File: "+SQL_FILE_NAME)






[Column.NAME,Column.PROVINCE,Column.ISTAT_CODE,Column.LATITUDE,Column.LONGITUDE]

CITIES_MAPPER = {
    "sigla_provincia": "province",
    "denominazione_ita": "name",
    "codice_istat": "istat_code",
    "lat": "latitude",
    "lon" : "longitude"
}