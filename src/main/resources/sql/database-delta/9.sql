--
-- Tag-Fragment
--
ALTER TABLE fragment ADD COLUMN tag_id BIGINT;
ALTER TABLE tag ADD COLUMN fragment_id BIGINT;
