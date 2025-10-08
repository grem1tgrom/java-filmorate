package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("UserDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::sqlRowToUser);
    }

    @Override
    public User addUser(User user) {
        UserValidator.validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sqlQuery = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            if (user.getBirthday() != null) {
                stmt.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!idIsPresent(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID " + user.getId() + " не найден в базе");
        }
        UserValidator.validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sqlQuery = "UPDATE users SET login = ?, email = ?, name = ?, birthday =? WHERE id = ?";
        jdbcTemplate.update(sqlQuery, user.getLogin(), user.getEmail(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public User findUserByID(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (userRow.next()) {
            User.UserBuilder userBuilder = User.builder()
                    .id(userRow.getInt("id"))
                    .login(userRow.getString("login"))
                    .email(userRow.getString("email"))
                    .name(userRow.getString("name"));
            Date birthday = userRow.getDate("birthday");
            if (birthday != null) {
                userBuilder.birthday(birthday.toLocalDate());
            }
            return userBuilder.build();
        } else {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден в базе");
        }
    }

    @Override
    public boolean addFriendship(Integer userID, Integer friendID) {
        if (!idIsPresent(userID) || !idIsPresent(friendID)) {
            throw new UserNotFoundException("Один из пользователей отсутствует в базе, регистрация дружбы невозможна");
        }
        String sqlQuery = "MERGE INTO friendship(user_id, friend_id) VALUES(?, ?)";
        int updatedRows = jdbcTemplate.update(sqlQuery, userID, friendID);
        return updatedRows > 0;
    }

    @Override
    public boolean removeFriendship(Integer userID, Integer friendID) {
        if (!idIsPresent(userID) || !idIsPresent(friendID)) {
            throw new UserNotFoundException("Один из пользователей отсутствует в базе, удаление дружбы невозможно");
        }
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int deletedRows = jdbcTemplate.update(sqlQuery, userID, friendID);
        return deletedRows > 0;
    }

    @Override
    public List<User> getFriendsOfUser(Integer id) {
        String sqlQuery = "SELECT u.* FROM users u JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::sqlRowToUser, id);
    }

    @Override
    public List<User> getFriendsCrossing(int userID, int anotherUserID) {
        String sqlQuery = "SELECT u.* FROM users u " +
                "JOIN friendship f1 ON u.id = f1.friend_id " +
                "JOIN friendship f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::sqlRowToUser, userID, anotherUserID);
    }


    @Override
    public boolean idIsPresent(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        return userRow.next();
    }

    @Override
    public void deleteUserByID(Integer id) {
        if (!idIsPresent(id)) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден в базе");
        }
        String sqlQuery = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private User sqlRowToUser(ResultSet resultSet, int rowNumber) throws SQLException {
        User.UserBuilder userBuilder = User.builder()
                .id(resultSet.getInt("id"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .name(resultSet.getString("name"));
        Date birthday = resultSet.getDate("birthday");
        if (birthday != null) {
            userBuilder.birthday(birthday.toLocalDate());
        }
        return userBuilder.build();
    }
}