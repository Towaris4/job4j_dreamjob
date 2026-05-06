CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    email    VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL
);