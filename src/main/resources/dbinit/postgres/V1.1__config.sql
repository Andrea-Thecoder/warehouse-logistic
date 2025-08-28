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

ALTER TABLE movementtrack
ADD CONSTRAINT chk_origin_or_destination_not_null
CHECK (
    origin_warehouse_id IS NOT NULL
    OR destination_warehouse_id IS NOT NULL
);

ALTER TABLE movementtrack
ADD CONSTRAINT chk_origin_dest_duration_distance
CHECK (
    origin_warehouse_id IS NULL
    OR destination_warehouse_id IS NULL
    OR (estimated_duration_millis IS NOT NULL AND estimated_distance_meters IS NOT NULL)
);