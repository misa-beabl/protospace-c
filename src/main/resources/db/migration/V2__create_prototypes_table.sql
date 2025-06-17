create TABLE prototypes (
  id serial primary KEY,
  user_id INT,
  name VARCHAR(256) not null,
  slogan VARCHAR(512) not null,
  concept VARCHAR(512) not null,
  image VARCHAR(256) not null,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
