package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = "SELECT * FROM films";
        List<Film> allFilms = jdbcTemplate.query(sqlQuery, this::sqlRowToFilm);
        log.debug("Сформирован список всех фильмов в базе размерностью {}", allFilms.size());
        return allFilms;
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, MPA_id) " +
                "VALUES(?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        saveGenresOfFilmInDB(film);
        log.debug("Добавлен фильм с ID {}, его название {}", film.getId(), film.getName());
        return getFilmByID(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        if (!idIsPresent(film.getId())) {
            throw new FilmNotFoundException("Фильм с ID - " + film.getId() + " не найден в базе");
        }
        String sqlQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "MPA_id = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        deleteGenresOfFilmInDB(film.getId());
        saveGenresOfFilmInDB(film);
        log.debug("Обновлен фильм с ID {}, его название {}", film.getId(), film.getName());
        return getFilmByID(film.getId());
    }

    @Override
    public Film getFilmByID(Integer id) {
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (filmRow.next()) {
            log.debug("Найден фильм с ID {}, его название {}", filmRow.getInt("id"), filmRow.getString("name"));
            return Film.builder()
                    .id(filmRow.getInt("id"))
                    .name(filmRow.getString("name"))
                    .description(filmRow.getString("description"))
                    .releaseDate(filmRow.getDate("release_date").toLocalDate())
                    .duration(filmRow.getInt("duration"))
                    .mpa(mpaStorage.getMpaByID(filmRow.getInt("mpa_id")))
                    .genres(getGenresOfFilmByID(filmRow.getInt("id")))
                    .likes(getLikesByFilmId(id))
                    .build();
        } else {
            throw new FilmNotFoundException("Фильм с ID - " + id + " не найден в базе");
        }
    }

    @Override
    public void deleteFilmByID(Integer id) {
        if (!idIsPresent(id)) {
            throw new FilmNotFoundException("Фильм с ID - " + id + " не найден в базе");
        }
        String sqlQuery = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.debug("Фильм с ID {} удален", id);
    }

    @Override
    public boolean addLike(Integer filmID, Integer userID) {
        String sqlQuery = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmID, userID);
        log.debug("Добавлен лайк от пользователя с ID {} к фильму с ID {}", userID, filmID);
        return true;
    }

    @Override
    public boolean removeLike(Integer filmID, Integer userID) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deletedEntity = jdbcTemplate.update(sqlQuery, filmID, userID);
        if (deletedEntity > 0) {
            log.debug("Удален лайк от пользователя с ID {} к фильму с ID {}", userID, filmID);
            return true;
        }
        log.debug("Лайк от пользователя с ID {} к фильму с ID {} отсутствует в базе", userID, filmID);
        return false;
    }

    @Override
    public boolean idIsPresent(Integer id) {
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        return filmRow.next();
    }

    @Override
    public List<Film> findTopLikedFilms(Integer count) {
        String sqlQuery = "SELECT f.* FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        List<Film> topFilms = jdbcTemplate.query(sqlQuery, this::sqlRowToFilm, count);
        log.debug("Сформирован список фильмов с наибольшим количеством лайков размерностью {}", topFilms.size());
        return topFilms;
    }

    private Set<Integer> getLikesByFilmId(Integer filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

    private void saveGenresOfFilmInDB(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            log.debug("Список жанров пуст, добавлять запись в БД не требуется");
            return;
        }
        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre currGenre : film.getGenres()) {
            jdbcTemplate.update(sqlQuery, film.getId(), currGenre.getId());
        }
        log.debug("В БД сохранено {} записей о жанрах фильма с ID {} и названием {}", film.getGenres().size(), film.getId(), film.getName());
    }

    private void deleteGenresOfFilmInDB(Integer filmID) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = ?";
        int deletedEntries = jdbcTemplate.update(sqlQuery, filmID);
        log.debug("{} записей о жанрах фильма с ID {} удалено из базы", deletedEntries, filmID);
    }

    private List<Genre> getGenresOfFilmByID(Integer filmID) {
        List<Genre> genresOfFilm = new ArrayList<>();
        String sqlQuery = "SELECT DISTINCT genre_id FROM film_genres WHERE film_id = ?";
        List<Integer> genresID = jdbcTemplate.queryForList(sqlQuery, Integer.class, filmID);
        if (genresID.isEmpty()) {
            log.debug("Сформированный список жанров для фильма с ID {} пуст", filmID);
        } else {
            for (int currGenreID : genresID) {
                genresOfFilm.add(genreStorage.getGenreByID(currGenreID));
            }
            log.info("Сформирован список жанров для фильма с ID {} размерностью {}", filmID, genresOfFilm.size());
        }
        return genresOfFilm;
    }

    private Film sqlRowToFilm(ResultSet resultSet, int rowNumber) throws SQLException {
        int filmId = resultSet.getInt("id");
        return Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(mpaStorage.getMpaByID(resultSet.getInt("MPA_id")))
                .genres(getGenresOfFilmByID(filmId))
                .likes(getLikesByFilmId(filmId))
                .build();
    }
}