-- liquibase formatted sql

-- changeset rat:0010-1
CREATE SCHEMA IF NOT EXISTS staging AUTHORIZATION jooq_demo_admin;
GRANT USAGE ON SCHEMA staging TO jooq_demo_user;
GRANT select,insert,update,delete ON ALL TABLES IN SCHEMA staging TO jooq_demo_user;
GRANT select,usage ON ALL SEQUENCES IN SCHEMA staging to jooq_demo_user;
GRANT execute ON ALL FUNCTIONS IN SCHEMA staging to jooq_demo_user;

ALTER DEFAULT PRIVILEGES FOR USER jooq_demo_admin IN SCHEMA staging GRANT select,insert,update,delete ON TABLES TO jooq_demo_user;
ALTER DEFAULT PRIVILEGES FOR USER jooq_demo_admin IN SCHEMA staging GRANT select,usage ON SEQUENCES TO jooq_demo_user;
ALTER DEFAULT PRIVILEGES FOR user jooq_demo_admin IN SCHEMA staging GRANT execute ON FUNCTIONS TO jooq_demo_user;

-- changeset rat:0010-2
CREATE TABLE IF NOT EXISTS staging.book
(
    isbn13    VARCHAR(17)  NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    author    VARCHAR(100) NOT NULL,
    title     VARCHAR(100) NOT NULL,
    genre     VARCHAR(100) NOT NULL,
    published DATE         NOT NULL,
    PRIMARY KEY (isbn13)
);

-- changeset rat:0010-3
CREATE TABLE IF NOT EXISTS staging.member
(
    id            SERIAL       NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    phone         VARCHAR(100),
    date_of_birth DATE         NOT NULL,
    PRIMARY KEY (id)
);

-- changeset rat:0010-4
CREATE TABLE IF NOT EXISTS staging.checkout
(
    id                 SERIAL      NOT NULL,
    member_id          INTEGER     NOT NULL,
    isbn13             VARCHAR(17) NOT NULL,
    checkout_date      DATE        NOT NULL,
    return_date        DATE        NOT NULL,
    actual_return_date DATE,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES staging.member (id),
    FOREIGN KEY (isbn13) REFERENCES staging.book (isbn13)
);


-- changeset rat:0010-5
CREATE TABLE IF NOT EXISTS staging.tmp_mapping_member_id_uuid(
    member_id INTEGER NOT NULL,
    uuid UUID NOT NULL,
    PRIMARY KEY (member_id)
);

-- changeset rat:0010-6
CREATE TABLE IF NOT EXISTS staging.tmp_mapping_book_isbn13_uuid(
    isbn13 VARCHAR(17) NOT NULL,
    uuid UUID NOT NULL,
    PRIMARY KEY (isbn13)
);
