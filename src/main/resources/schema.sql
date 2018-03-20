DROP TABLE IF EXISTS execution;


CREATE TABLE execution (
  file_hash bigint PRIMARY KEY,
  exec_time timestamp NOT NULL,
  label varchar(100) NOT NULL,
  score decimal NOT NULL
);