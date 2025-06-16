create TABLE prototypes (
  id serial primary KEY,
  user_id INT,
  name VARCHAR(256),
  slogan VARCHAR(512),
  concept VARCHAR(512),
  image VARCHAR(256),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
