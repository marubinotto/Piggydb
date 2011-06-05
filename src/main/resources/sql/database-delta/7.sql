--
-- Knowledge Fragment
-- 
ALTER TABLE fragment ADD COLUMN password VARCHAR(100);

--
-- Creator and Updater of Entity
-- 

ALTER TABLE fragment ADD COLUMN creator VARCHAR(200);
ALTER TABLE fragment ADD COLUMN updater VARCHAR(200);

ALTER TABLE tag ADD COLUMN creator VARCHAR(200);
ALTER TABLE tag ADD COLUMN updater VARCHAR(200);

ALTER TABLE filter ADD COLUMN creator VARCHAR(200);
ALTER TABLE filter ADD COLUMN updater VARCHAR(200);

ALTER TABLE fragment_relation ADD COLUMN creator VARCHAR(200);
ALTER TABLE fragment_relation ADD COLUMN updater VARCHAR(200);

--
-- Re-create full text search index for the table "fragment"
-- 
CALL FT_REINDEX();
