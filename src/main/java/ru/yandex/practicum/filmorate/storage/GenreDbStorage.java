package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return new Genre(resultSet.getInt("id"), resultSet.getString("name"));
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    @Override
    public List<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres AS g "
                + "JOIN film_genres AS fg ON g.id = fg.genre_id "
                + "WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    @Override
    public void updateFilmGenres(int filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres == null || genres.isEmpty()) {
            return;
        }

        genres.stream()
                .distinct()
                .collect(Collectors.toList())
                .forEach(genre -> {
                    String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                    jdbcTemplate.update(insertSql, filmId, genre.getId());
                });
    }
}