package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;

public class UserValidator {
    public static void validate(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин пользователя не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
    }
}