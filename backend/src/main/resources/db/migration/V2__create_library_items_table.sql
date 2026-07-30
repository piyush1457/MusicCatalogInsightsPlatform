CREATE TABLE IF NOT EXISTS library_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    apple_catalog_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    genre VARCHAR(255),
    release_date VARCHAR(50),
    duration_seconds INTEGER,
    artwork_url VARCHAR(500),
    user_rating INTEGER CHECK (user_rating IS NULL OR (user_rating >= 1 AND user_rating <= 5)),
    user_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_library_items_user_id ON library_items(user_id);
