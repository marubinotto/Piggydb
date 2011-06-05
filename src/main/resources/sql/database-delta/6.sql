--
-- Create full text search index for the table "fragment"
-- 
CALL FT_CREATE_INDEX('PUBLIC', 'FRAGMENT', 
'TITLE,CONTENT,CREATION_DATETIME,UPDATE_DATETIME,FILE_NAME,FILE_TYPE');
