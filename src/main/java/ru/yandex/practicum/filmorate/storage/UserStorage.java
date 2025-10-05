package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(int id);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Set<Integer> getFriendsIds(int userId);
}