--
-- Fragment Relation
-- 
CREATE TABLE fragment_relation (
  fragment_relation_id IDENTITY NOT NULL,

  from_id BIGINT NOT NULL,
  to_id BIGINT NOT NULL,

  creation_datetime TIMESTAMP NOT NULL,
  update_datetime TIMESTAMP NOT NULL,

  PRIMARY KEY(fragment_relation_id),
  UNIQUE(from_id, to_id)
);

CREATE SEQUENCE seq_fragment_relation_id START WITH 1;

