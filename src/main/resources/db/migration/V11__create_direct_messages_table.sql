CREATE TABLE direct_messages (
    id SERIAL PRIMARY KEY,
    text VARCHAR(1000) NOT NULL,
    sender_user_id INTEGER NOT NULL,
    receiver_user_id INTEGER NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_user_id) REFERENCES users(id) ON DELETE CASCADE
);