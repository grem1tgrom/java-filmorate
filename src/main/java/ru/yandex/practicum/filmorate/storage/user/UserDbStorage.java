package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("UserDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private int nextID;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        nextID = getMaxIdFromDb() + 1;
    }

    @Override
    public List<User> getAllUsers() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::sqlRowToUser);
    }

    @Override
    public User addUser(User user) {
        user.setId(nextID++);
        String sqlQuery = "INSERT INTO users(id, email, login, name, birthday) VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!idIsPresent(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID " + user.getId() + " не найден в базе");
        }
        String sqlQuery = "UPDATE users SET login = ?, email = ?, name = ?, birthday =? WHERE id = ?";
        jdbcTemplate.update(sqlQuery, user.getLogin(), user.getEmail(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public void deleteUserByID(Integer id) {
        // Not implemented
    }

    @Override
    public User findUserByID(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (userRow.next()) {
            return User.builder()
                    .id(userRow.getInt("id"))
                    .login(userRow.getString("login"))
                    .email(userRow.getString("email"))
                    .name(userRow.getString("name"))
                    .birthday(userRow.getDate("birthday").toLocalDate())
                    .friends(getFriendsByUserId(id))
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
            return false;
        }
        String sqlQuery = "INSERT INTO friendship(user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sqlQuery, userID, friendID);
        return true;
    }

    @Override
    public boolean removeFriendship(Integer userID, Integer friendID) {
        if (!idIsPresent(userID) || !idIsPresent(friendID)) {
            throw new UserNotFoundException("Один из пользователей отсутствует в базе, удаление дружбы невозможно");
        }
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        return jdbcTemplate.update(sqlQuery, userID, friendID) > 0;
    }

    @Override
    public List<User> getFriendsOfUser(Integer id) {
        List<User> friends = new ArrayList<>();
        String sqlQuery = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Integer> friendsID = jdbcTemplate.queryForList(sqlQuery, Integer.class, id);
        for (int currFriendID : friendsID) {
            friends.add(findUserByID(currFriendID));
        }
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
        return crossedFriends;
    }

    @Override
    public boolean idIsPresent(Integer id) {
        SqlRowSet userRow = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        return userRow.next();
    }

    private User sqlRowToUser(ResultSet resultSet, int rowNumber) throws SQLException {
        int userId = resultSet.getInt("id");
        return User.builder()
                .id(userId)
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .friends(getFriendsByUserId(userId))
                .build();
    }

    private Set<Integer> getFriendsByUserId(Integer id) {
        String sqlQuery = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Integer> friends = jdbcTemplate.queryForList(sqlQuery, Integer.class, id);
        return new HashSet<>(friends);
    }

    private Integer getMaxIdFromDb() {
        SqlRowSet maxIdRow = jdbcTemplate.queryForRowSet("SELECT MAX(id) AS max_id FROM users");
        if (maxIdRow.next()) {
            return maxIdRow.getInt("max_id");
        }
        return 0;
    }
}