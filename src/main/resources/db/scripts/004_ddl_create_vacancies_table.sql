CREATE TABLE vacancies
(
    id            SERIAL PRIMARY KEY,
    title         VARCHAR NOT NULL,
    description   VARCHAR NOT NULL,
    creation_date TIMESTAMP,
    visible       BOOLEAN NOT NULL,
    city_id       INT REFERENCES cities(id),
    file_id       INT REFERENCES files(id)
);