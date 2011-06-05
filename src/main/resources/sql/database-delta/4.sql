--
-- Knowledge Fragment
-- 
ALTER TABLE fragment ADD COLUMN children_ordered_by TINYINT NOT NULL DEFAULT 1;
ALTER TABLE fragment ADD COLUMN children_ordered_in_asc BOOLEAN NOT NULL DEFAULT FALSE;
