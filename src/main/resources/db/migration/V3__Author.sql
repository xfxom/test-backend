CREATE TABLE author (
    id serial PRIMARY KEY,
    full_name text NOT NULL,
    created_at timestamp NOT NULL DEFAULT now()
);

-- Add author to budget (optional)
ALTER TABLE budget
ADD COLUMN author_id int DEFAULT NULL,
ADD CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES author(id);