DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS bookings;

DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  description VARCHAR(2000) NOT NULL,
  requester_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
  is_available BOOLEAN NOT NULL,
  request_id BIGINT REFERENCES requests (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  item_id BIGINT REFERENCES items (id) ON DELETE CASCADE,
  booker_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
  status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  text VARCHAR(2000) NOT NULL,
  item_id BIGINT REFERENCES items (id) ON DELETE CASCADE,
  author_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
