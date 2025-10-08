package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlQuery = "SELECT * FROM genres";
        List<Genre> result = jdbcTemplate.query(sqlQuery, GenreDbStorage::makeGenre);
        log.info("Сформирован список всех жанров в базе размерностью {}", result.size());
        return result;
    }

    @Override
    public Genre getGenreByID(int id) {
        final String sqlQuery = "SELECT * FROM GENRES WHERE id = ?";
        final List<Genre> genres = jdbcTemplate.query(sqlQuery, GenreDbStorage::makeGenre, id);
        if (genres.size() != 1) {
            throw new GenreNotFoundException("Жанр с ID " + id + " не найден в базе");
        }
        return genres.get(0);
    }

    static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}