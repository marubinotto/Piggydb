--
-- Add prefix '#' to special tags
-- 
update tag set tag_name = CONCAT('#', tag_name), update_datetime = CURRENT_TIMESTAMP() 
where (
  tag_name in ('trash', 'home', 'bookmark', 'pre', 'code')
  or tag_name like 'lang-%'
)
and (
  not exists (select * from tag 
    where tag_name in ('#trash', '#home', '#bookmark', '#pre', '#code')
      or tag_name like '#lang-%')
);
