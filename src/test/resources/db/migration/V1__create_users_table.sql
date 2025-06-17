CREATE TABLE users (
   id SERIAL    NOT NULL,
   nickname VARCHAR(128)    NOT NULL,
   email VARCHAR(128)    NOT NULL UNIQUE,
   password VARCHAR(512)    NOT NULL,
   profile VARCHAR(512)    NOT NULL,
   affiliation varchar(64)    NOT NULL,
   position varchar(32)    NOT NULL,
   PRIMARY KEY (id)
);