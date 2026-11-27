-- ==============================================================================
-- MIGRACIÓN V3: Preferencia visual de borde para el logo del comercio
-- ==============================================================================

ALTER TABLE tenants 
ADD COLUMN IF NOT EXISTS logo_has_border BOOLEAN NOT NULL DEFAULT TRUE;
