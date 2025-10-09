CREATE TABLE IF NOT EXISTS mpa (
                                   id INTEGER PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
                                      id INTEGER PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
                                     id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
                                     description VARCHAR(200) NOT NULL,
                                     release_date DATE NOT NULL,
                                     duration INTEGER CHECK (duration > 0),
                                     mpa_id INTEGER REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS film_genres (
                                           film_id INTEGER REFERENCES films(id),
                                           genre_id INTEGER REFERENCES genres(id),
                                           PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     login VARCHAR(255) NOT NULL UNIQUE,
                                     name VARCHAR(255) NOT NULL,
                                     birthday DATE
);

CREATE TABLE IF NOT EXISTS friendship (
                                          user_id INTEGER REFERENCES users(id),
                                          friend_id INTEGER REFERENCES users(id),
                                          PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS likes (
                                     film_id INTEGER REFERENCES films(id),
                                     user_id INTEGER REFERENCES users(id),
                                     PRIMARY KEY (film_id, user_id)
);