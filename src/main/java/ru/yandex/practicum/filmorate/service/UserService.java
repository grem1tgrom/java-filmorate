package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage storage) {
        this.userStorage = storage;
    }

    public User addUser(User user) {
        UserValidator.validate(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        UserValidator.validate(user);
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserByID(int id) {
        return userStorage.findUserByID(id);
    }

    public String addFriendship(int userID, int friendID) {
        boolean success = userStorage.addFriendship(userID, friendID);
        if (success) {
            return String.format("Пользователь с ID %d успешно добавлен в друзья к пользователю с ID %d", friendID, userID);
        } else {
            return String.format("Пользователь с ID %d уже дружит с пользователем, ID %d", friendID, userID);
        }
    }

    public String removeFriendship(int userID, int friendID) {
        boolean success = userStorage.removeFriendship(userID, friendID);
        if (success) {
            return String.format("Пользователь с ID %d успешно удален из друзей у пользователя с ID %d", friendID, userID);
        } else {
            return String.format("Пользователь с ID %d уже отсутствует в друзьях у пользователя, ID %d", friendID, userID);
        }
    }

    public List<User> getFriendsOfUser(int userID) {
        log.info("Запрошен список друзей у пользователя с ID {}", userID);
        return userStorage.getFriendsOfUser(userID);
    }

    public List<User> getFriendsCrossing(int userID, int anotherUserID) {
        log.info("Запрошен список общих друзей у пользователей ID {} и {}", userID, anotherUserID);
        return userStorage.getFriendsCrossing(userID, anotherUserID);
    }

    public boolean checkUserIdInStorage(Integer id) {
        log.info("Запрошена проверка ID {} в базе пользователей", id);
        if (!userStorage.idIsPresent(id)) {
            throw new UserNotFoundException("Пользователь с ID - " + id + " не найден в базе");
        }
        return true;
    }
}