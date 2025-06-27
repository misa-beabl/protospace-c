ALTER TABLE prototypes ADD COLUMN genre_id INT;

ALTER TABLE prototypes ADD CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES genres(id);