-- Создание расширения для работы с векторами
CREATE EXTENSION IF NOT EXISTS vector;

-- RAG
-- Таблица загруженных документов
CREATE TABLE IF NOT EXISTS loaded_document
(
    id            SERIAL PRIMARY KEY,
    filename      VARCHAR(255) NOT NULL,
    content_hash  VARCHAR(64)  NOT NULL,
    document_type VARCHAR(10)  NOT NULL,
    chunk_count   INTEGER,
    loaded_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_document UNIQUE (filename, content_hash)
);

-- Индекс для быстрого поиска по имени файла
CREATE INDEX IF NOT EXISTS idx_loaded_documents_filename
    ON loaded_document (filename);

-- Таблица для векторного хранилища
CREATE TABLE IF NOT EXISTS vector_store
(
    id        VARCHAR(255) PRIMARY KEY,
    content   TEXT,
    metadata  JSON,
    embedding VECTOR(1024)
);

-- Индекс HNSW для быстрого векторного поиска
CREATE INDEX IF NOT EXISTS vector_store_hnsw_index
    ON vector_store USING hnsw (embedding vector_cosine_ops);