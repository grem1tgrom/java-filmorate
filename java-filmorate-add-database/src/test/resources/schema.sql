CREATE TABLE IF NOT EXISTS mpa_ratings (
                                           id INTEGER PRIMARY KEY,
                                           name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS genres (
                                      id INTEGER PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     login VARCHAR(255) NOT NULL UNIQUE,
                                     name VARCHAR(255),
                                     birthday DATE
);

CREATE TABLE IF NOT EXISTS films (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
                                     description VARCHAR(200),
                                     release_date DATE,
                                     duration INTEGER,
                                     mpa_rating_id INTEGER,
                                     FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS film_genres (
                                           film_id INTEGER REFERENCES films(id) ON DELETE CASCADE,
                                           genre_id INTEGER REFERENCES genres(id) ON DELETE RESTRICT,
                                           PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS likes (
                                     film_id INTEGER REFERENCES films(id) ON DELETE CASCADE,
                                     user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                                     PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friendships (
                                           user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                                           friend_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                                           PRIMARY KEY (user_id, friend_id),
                                           CHECK (user_id <> friend_id)
);