--
-- Filter
-- 
ALTER TABLE filter ADD COLUMN includes_and BOOLEAN NOT NULL DEFAULT TRUE;