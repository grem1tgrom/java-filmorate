package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
        List<Genre> result = jdbcTemplate.query(sqlQuery, this::sqlRowToGenre);
        log.info("Сформирован список всех жанров в базе размерностью {}", result.size());
        return result;
    }

    @Override
    public Genre getGenreByID(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?", id);
        if (genreRows.next()) {
            log.debug("Найден жанр с ID {}, его название {}", genreRows.getInt("id"), genreRows.getString("name"));
            return Genre.builder()
                    .id(genreRows.getInt("id"))
                    .name(genreRows.getString("name"))
                    .build();
        } else {
            throw new GenreNotFoundException("Жанр с ID " + id + " не найден в базе");
        }
    }

    private Genre sqlRowToGenre(ResultSet resultSet, int rowNumber) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}