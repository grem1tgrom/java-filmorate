package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTests {
    static Validator validator;
    UserController controller;
    User user;

    @BeforeAll
    public static void start() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void beforeEach() {
        UserStorage userStorage = new InMemoryUserStorage();
        controller = new UserController(new UserService(userStorage));
        user = User.builder()
                .email("123@123.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
    }

    @Test
    void correctSaveUser() {
        controller.create(user);
        assertEquals(1, user.getId());
    }

    @Test
    void correctSaveSeveralUsers() {
        controller.create(user);
        controller.create(user);
        controller.create(user);
        controller.create(user);
        controller.create(user);
        controller.create(user);
        controller.create(user);
        assertEquals(7, controller.getUsers().size());
    }

    @Test
    void getValidationExceptionWhenEmailIncorrect() {
        user.setEmail("dfghjkasdj.ru");
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Передана некорректная почта", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenLoginIsNull() {
        user.setLogin(null);
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Логин пользователя не может быть пустым", error.getMessage());
    }

    @Test
    void getExceptionWhenSaveUserAndLoginWithBlank() {
        user.setLogin("my new login");
        final ValidationException exception = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Логин пользователя не может содержать пробелы", exception.getMessage());
    }

    @Test
    void nameEqualsLoginWhenNameIsNull() {
        user.setName(null);
        controller.create(user);
        assertEquals("testLogin", user.getName());
    }

    @Test
    void nameEqualsLoginWhenNameIsBlank() {
        user.setName("         ");
        controller.create(user);
        assertEquals("testLogin", user.getName());
    }

    @Test
    void getExceptionWhenSaveUserAndBirthdayInFuture() {
        user.setBirthday(LocalDate.of(2120, 6, 13));
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Введена дата рождения в будущем", error.getMessage());
    }

    @Test
    void correctUpdateUser() {
        controller.create(user);
        User update = User.builder()
                .id(user.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name("updatedName")
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.update(update);
        assertEquals(update.getLogin(), controller.getUsers().get(controller.getUsers().size() - 1).getLogin());
    }

    @Test
    void correctUpdateUserWhenNameIsNull() {
        controller.create(user);
        User update = User.builder()
                .id(user.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name(null)
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.update(update);
        assertEquals(update.getLogin(), controller.getUsers().get(controller.getUsers().size() - 1).getName());
    }

    @Test
    void getExceptionWhenUpdateUserAndLoginWithBlank() {
        controller.create(user);
        User update = User.builder()
                .id(user.getId())
                .email("updated@update.com")
                .login("updated new login")
                .name("updatedName")
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        final ValidationException exception = assertThrows(ValidationException.class, () -> controller.update(update));
        assertEquals("Логин пользователя не может содержать пробелы", exception.getMessage());
    }
}