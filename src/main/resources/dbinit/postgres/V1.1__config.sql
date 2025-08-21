CREATE EXTENSION IF NOT EXISTS pg_trgm;

--CREATE INDEX idx_recipe_sort_name_trgm
--ON recipe
--USING gin (sort_name gin_trgm_ops);

CREATE INDEX idx_city_name_trgm
ON city
USING gin (name gin_trgm_ops);

CREATE INDEX idx_city_istat_code_trgm
ON city
USING gin (istat_code gin_trgm_ops);

CREATE INDEX idx_warehouse_name_trgm
ON warehouse
USING gin (name gin_trgm_ops);