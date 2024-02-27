-- liquibase formatted sql
-- changeset rat:0020-5
SET SEARCH_PATH TO jooq_demo, extensions;
CREATE TABLE IF NOT EXISTS book
(
    id          uuid         NOT NULL    DEFAULT uuid_generate_v7(),
    isbn13      VARCHAR(17)  NOT NULL,
    publisher   VARCHAR(100) NOT NULL,
    author      VARCHAR(100) NOT NULL,
    title       VARCHAR(100) NOT NULL,
    genre       VARCHAR(100) NOT NULL,
    published   DATE         NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    created_by  VARCHAR(50)  NOT NULL    DEFAULT current_user,
    modified_at TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    modified_by VARCHAR(50)  NOT NULL    DEFAULT current_user,
    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uq_book_isbn13 UNIQUE (isbn13)
);

-- changeset rat:0020-6
CREATE TABLE IF NOT EXISTS member
(
    id            uuid         NOT NULL    DEFAULT uuid_generate_v7()
        CONSTRAINT pk_member PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(254) NOT NULL
        CONSTRAINT uq_member_email UNIQUE,
    phone         VARCHAR(100),
    date_of_birth DATE         NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    created_by    VARCHAR(100) NOT NULL    DEFAULT current_user,
    modified_at   TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    modified_by   VARCHAR(100) NOT NULL    DEFAULT current_user,
    CONSTRAINT cc_member_date_of_birth CHECK ( date_of_birth <= current_date AND date_of_birth >= (current_date - INTERVAL '150 years')::DATE)
);
COMMENT ON COLUMN member.email IS 'Email addresses must be unique, but are not a primary key, because they may change over time.';

-- changeset rat:0020-7
CREATE TABLE IF NOT EXISTS instance
(
    id                            uuid         NOT NULL    DEFAULT uuid_generate_v7(),
    book_id                       uuid         NOT NULL,
    acquired_date                 DATE         NOT NULL,
    removed_from_circulation_date DATE,
    comments                      VARCHAR[300][],
    created_at                    TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    created_by                    VARCHAR(100) NOT NULL    DEFAULT current_user,
    modified_at                   TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    modified_by                   VARCHAR(100) NOT NULL    DEFAULT current_user,
    CONSTRAINT fk_instance_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT pk_instance PRIMARY KEY (id)
);

-- changeset rat:0020-8
CREATE TABLE IF NOT EXISTS checkout
(
    id                      uuid                     DEFAULT uuid_generate_v7() NOT NULL,
    member_id               uuid         NOT NULL,
    instance_id             uuid         NOT NULL,
    checkout_date           DATE         NOT NULL,
    return_date             DATE         NOT NULL,
    planned_checkout_period daterange GENERATED ALWAYS AS (daterange(checkout_date, return_date, '[]')) STORED,
    actual_return_date      DATE,
    actual_checkout_period  daterange GENERATED ALWAYS AS (daterange(checkout_date, actual_return_date, '[]')) STORED,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    created_by              VARCHAR(100) NOT NULL    DEFAULT current_user,
    modified_at             TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    modified_by             VARCHAR(100) NOT NULL    DEFAULT current_user,
    CONSTRAINT pk_checkout PRIMARY KEY (id),
    CONSTRAINT fk_checkout_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_checkout_instance FOREIGN KEY (instance_id) REFERENCES instance (id),
    CONSTRAINT ec_checkouts_no_overlaps EXCLUDE USING gist (instance_id gist_uuid_ops WITH =, planned_checkout_period WITH &&)
);
