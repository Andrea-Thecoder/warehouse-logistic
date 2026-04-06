SET search_path TO dev, public;
CREATE EXTENSION IF NOT EXISTS pg_trgm;


CREATE INDEX idx_city_name_trgm
ON city
USING gin (name gin_trgm_ops);

CREATE INDEX idx_city_istat_code_trgm
ON city
USING gin (istat_code gin_trgm_ops);

CREATE INDEX idx_warehouse_name_trgm
ON warehouse
USING gin (name gin_trgm_ops);

ALTER TABLE movement_track
ADD CONSTRAINT chk_origin_duration_distance
CHECK (
    origin_warehouse_id IS NULL
    OR (estimated_total_duration_millis IS NOT NULL AND estimated_total_distance_meters IS NOT NULL)
);