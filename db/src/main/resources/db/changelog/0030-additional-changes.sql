-- liquibase formatted sq
-- changeset rat:0030-01 context:@demo-1
CREATE INDEX IF NOT EXISTS idx_book_author ON book (author);
-- rollback DROP INDEX IF EXISTS idx_book_author;

-- changeset rat:0030-02 context:@demo-2
ALTER TABLE checkout RENAME COLUMN return_date TO borrowed_until_date;
-- rollback ALTER TABLE checkout RENAME COLUMN borrowed_until_date TO return_date;
