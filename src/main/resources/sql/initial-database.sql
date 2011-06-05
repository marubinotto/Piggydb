--
-- Global Setting
-- 
CREATE TABLE global_setting (
  setting_name VARCHAR(100) NOT NULL,
  setting_value VARCHAR,

  PRIMARY KEY(setting_name)
);

INSERT INTO global_setting (setting_name, setting_value) VALUES('database.version', '1');


--
-- Tag
-- 
CREATE TABLE tag (
  tag_id BIGINT NOT NULL,

  tag_name VARCHAR(100) NOT NULL UNIQUE,

  creation_datetime TIMESTAMP NOT NULL,
  update_datetime TIMESTAMP NOT NULL,

  PRIMARY KEY(tag_id)
);

CREATE SEQUENCE seq_tag_id START WITH 1;


--
-- Tagging
-- 
CREATE TABLE tagging (
  tagging_id IDENTITY NOT NULL,

  tag_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  target_type TINYINT NOT NULL,	-- 1:tag, 2:fragment, 3:filter-classification, 4:filter-excludes

  PRIMARY KEY(tagging_id),
  UNIQUE(tag_id, target_id, target_type)
);


--
-- Knowledge Fragment
-- 
CREATE TABLE fragment (
  fragment_id BIGINT NOT NULL,

  title VARCHAR(200),
  content VARCHAR,

  creation_datetime TIMESTAMP NOT NULL,
  update_datetime TIMESTAMP NOT NULL,

  PRIMARY KEY(fragment_id)
);

CREATE SEQUENCE seq_fragment_id START WITH 1;


--
-- Filter
-- 
CREATE TABLE filter (
  filter_id BIGINT NOT NULL,

  filter_name VARCHAR(100) NOT NULL UNIQUE,

  creation_datetime TIMESTAMP NOT NULL,
  update_datetime TIMESTAMP NOT NULL,

  PRIMARY KEY(filter_id)
);

CREATE SEQUENCE seq_filter_id START WITH 1;

