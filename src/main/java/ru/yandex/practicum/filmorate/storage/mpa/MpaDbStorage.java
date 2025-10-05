package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MPA> getAllMpa() {
        String sqlQuery = "SELECT * FROM mpa";
        List<MPA> result = jdbcTemplate.query(sqlQuery, this::sqlRowToMPA);
        log.info("Сформирован список всех рейтингов в базе размерностью {}", result.size());
        return result;
    }

    @Override
    public MPA getMpaByID(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa WHERE id = ?", id);
        if (mpaRows.next()) {
            log.debug("Найден рейтинг с ID {}, его название {}", mpaRows.getInt("id"), mpaRows.getString("name"));
            return MPA.builder()
                    .id(mpaRows.getInt("id"))
                    .name(mpaRows.getString("name"))
                    .build();
        } else {
            throw new MpaNotFoundException("Рейтинг с ID " + id + " не найден в базе");
        }
    }

    private MPA sqlRowToMPA(ResultSet resultSet, int rowNumber) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}