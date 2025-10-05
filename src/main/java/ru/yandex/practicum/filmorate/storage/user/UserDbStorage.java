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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
        List<User> allUsers = jdbcTemplate.query(sqlQuery, this::sqlRowToUser);
        log.debug("Сформирован список всех пользователей в базе размерностью {}", allUsers.size());
        return allUsers;
    }

    @Override
    public User addUser(User user) {
        String sqlQuery = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(newId);

        log.debug("Добавлен пользователь с ID {}, его логин {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!idIsPresent(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID " + user.getId() + " не найден в базе");
        }
        String sqlQuery = "UPDATE users SET login = ?, email = ?, name = ?, birthday =? WHERE id = ?";
        jdbcTemplate.update(sqlQuery, user.getLogin(), user.getEmail(), user.getName(), user.getBirthday(), user.getId());
        log.debug("Пользователь с ID {} и логином {} обновил данные", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void deleteUserByID(Integer id) {
        String sqlQuery = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public User findUserByID(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (userRow.next()) {
            log.debug("Найден пользователь с ID {}, его логин {}", userRow.getInt("id"), userRow.getString("login"));
            return User.builder()
                    .id(userRow.getInt("id"))
                    .login(userRow.getString("login"))
                    .email(userRow.getString("email"))
                    .name(userRow.getString("name"))
                    .birthday(Objects.requireNonNull(userRow.getDate("birthday")).toLocalDate())
                    .friends(new HashSet<>()) // Друзей подгружаем отдельно
                    .build();
        } else {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден в базе");
        }
    }

    @Override
    public boolean addFriendship(Integer userID, Integer friendID) {
        if (!idIsPresent(userID) || !idIsPresent(friendID)) {
            throw new UserNotFoundException("Один из пользователей отсутствует в базе, регистрация дружбы невозможна");
        }
        SqlRowSet friendshipRow = jdbcTemplate.queryForRowSet("SELECT * FROM friendship WHERE user_id = ? AND friend_id = ?",
                userID, friendID);
        if (friendshipRow.next()) {
            log.debug("Пользователь с ID {} уже находится в друзьях у пользователя с ID {}", friendID, userID);
            return false;
        }
        String sqlQuery = "INSERT INTO friendship(user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sqlQuery, userID, friendID);
        log.debug("Пользователь с ID {} успешно добавлен в друзья к пользователю с ID {}", friendID, userID);
        return true;
    }

    @Override
    public boolean removeFriendship(Integer userID, Integer friendID) {
        if (!idIsPresent(userID) || !idIsPresent(friendID)) {
            throw new UserNotFoundException("Один из пользователей отсутствует в базе, удаление дружбы невозможно");
        }
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        if (jdbcTemplate.update(sqlQuery, userID, friendID) > 0) {
            log.debug("Пользователь с ID {} успешно удален из друзей у пользователя с ID {}", friendID, userID);
            return true;
        }
        log.debug("Пользователь с ID {} отсутствует в друзьях у пользователя с ID {}", friendID, userID);
        return false;
    }

    @Override
    public List<User> getFriendsOfUser(Integer id) {
        if (!idIsPresent(id)) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        List<User> friends = new ArrayList<>();
        String sqlQuery = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Integer> friendsID = jdbcTemplate.queryForList(sqlQuery, Integer.class, id);
        for (int currFriendID : friendsID) {
            friends.add(findUserByID(currFriendID));
        }
        log.debug("Сформирован список друзей для пользователя с ID {} размерностью {}", id, friends.size());
        return friends;
    }

    @Override
    public List<User> getFriendsCrossing(int userID, int anotherUserID) {
        List<User> crossedFriends = new ArrayList<>();
        String sqlQuery = "SELECT friend_id FROM friendship WHERE user_id = ? " +
                "AND friend_id IN (SELECT friend_id FROM friendship WHERE user_id = ?)";
        List<Integer> crossedFriendsID = jdbcTemplate.queryForList(sqlQuery, Integer.class, userID, anotherUserID);
        for (int currFriendID : crossedFriendsID) {
            crossedFriends.add(findUserByID(currFriendID));
        }
        log.debug("Сформирован список общих друзей для пользователей с ID {} и {} размерностью {}", userID, anotherUserID, crossedFriends.size());
        return crossedFriends;
    }

    @Override
    public boolean idIsPresent(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        return userRow.next();
    }

    private User sqlRowToUser(ResultSet resultSet, int rowNumber) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .friends(new HashSet<>())
                .build();
    }
}