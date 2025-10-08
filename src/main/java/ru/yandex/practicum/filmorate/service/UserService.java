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

    public void addFriendship(int userID, int friendID) {
        userStorage.addFriendship(userID, friendID);
        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userID, friendID);
    }

    public void removeFriendship(int userID, int friendID) {
        userStorage.removeFriendship(userID, friendID);
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userID, friendID);
    }

    public List<User> getFriendsOfUser(int userID) {
        if (!userStorage.idIsPresent(userID)) {
            throw new UserNotFoundException("Пользователь с ID " + userID + " не найден в базе");
        }
        log.info("Запрошен список друзей у пользователя с ID {}", userID);
        return userStorage.getFriendsOfUser(userID);
    }

    public List<User> getFriendsCrossing(int userID, int anotherUserID) {
        log.info("Запрошен список общих друзей у пользователей ID {} и {}", userID, anotherUserID);
        return userStorage.getFriendsCrossing(userID, anotherUserID);
    }

    public boolean checkUserIdInStorage(Integer id) {
        log.info("Запрошена проверка ID {} в базе пользователей", id);
        return userStorage.idIsPresent(id);
    }
}