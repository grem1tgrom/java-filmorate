package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("filmDbStorage")
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

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        int mpaId = resultSet.getInt("mpa_rating_id");
        Optional<Mpa> mpa = mpaStorage.getMpaRatingById(mpaId);
        mpa.ifPresent(film::setMpa);

        String sqlLikes = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.query(sqlLikes, (rs, rowN) -> rs.getInt("user_id"), film.getId());
        film.getLikes().addAll(likes);

        film.getGenres().addAll(genreStorage.getFilmGenres(film.getId()));

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT id, name, description, release_date, duration, mpa_rating_id FROM films";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film addFilm(Film film) {
        String sqlMpaCheck = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlMpaCheck, Integer.class, film.getMpa().getId());
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден.");
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int id = simpleJdbcInsert.executeAndReturnKey(Map.of(
                "name", film.getName(),
                "description", film.getDescription(),
                "release_date", film.getReleaseDate(),
                "duration", film.getDuration(),
                "mpa_rating_id", film.getMpa().getId()
        )).intValue();
        film.setId(id);

        genreStorage.updateFilmGenres(film.getId(), new ArrayList<>(film.getGenres()));
        return getFilmById(film.getId()).orElse(null);
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден."));

        String sqlMpaCheck = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlMpaCheck, Integer.class, film.getMpa().getId());
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден.");
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        genreStorage.updateFilmGenres(film.getId(), new ArrayList<>(film.getGenres()));
        return getFilmById(film.getId()).orElse(null);
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "SELECT id, name, description, release_date, duration, mpa_rating_id FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        return films.stream().findFirst();
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        if (jdbcTemplate.update(sql, filmId, userId) == 0) {
            throw new NotFoundException(String.format("Лайк от пользователя с id=%d на фильм id=%d не найден.", userId, filmId));
        }
    }
}