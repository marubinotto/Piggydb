--
-- Knowledge Fragment
-- 
ALTER TABLE fragment ADD COLUMN file_name VARCHAR(200);
ALTER TABLE fragment ADD COLUMN file_type VARCHAR(30);
ALTER TABLE fragment ADD COLUMN file_size BIGINT;
