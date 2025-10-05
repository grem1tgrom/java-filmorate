package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        controller.createUser(user);
        assertEquals(1, user.getId());
    }

    @Test
    void correctSaveSeveralUsers() {
        controller.createUser(user);
        controller.createUser(user);
        controller.createUser(user);
        controller.createUser(user);
        controller.createUser(user);
        controller.createUser(user);
        controller.createUser(user);
        assertEquals(7, controller.getAllUsers().size());
    }

    @Test
    void getValidationExceptionWhenEmailIncorrect() {
        user.setEmail("dfghjkasdj.ru");
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Некорректный формат email.", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenLoginIsNull() {
        user.setLogin(null);
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Логин не может быть пустым.", error.getMessage());
    }

    @Test
    void nameEqualsLoginWhenNameIsNull() {
        user.setName(null);
        controller.createUser(user);
        assertEquals("testLogin", user.getName());
    }

    @Test
    void nameEqualsLoginWhenNameIsBlank() {
        user.setName("         ");
        controller.createUser(user);
        assertEquals("testLogin", user.getName());
    }

    @Test
    void getExceptionWhenSaveUserAndBirthdayInFuture() {
        user.setBirthday(LocalDate.of(2120, 6, 13));
        Set<ConstraintViolation<User>> errors = validator.validate(user);
        ConstraintViolation<User> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Дата рождения не может быть в будущем.", error.getMessage());
    }

    @Test
    void correctUpdateUser() {
        controller.createUser(user);
        User update = User.builder()
                .id(user.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name("updatedName")
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.updateUser(update);
        assertEquals(update.getLogin(), controller.getAllUsers().get(controller.getAllUsers().size() - 1).getLogin());
    }

    @Test
    void correctUpdateUserWhenNameIsNull() {
        controller.createUser(user);
        User update = User.builder()
                .id(user.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name(null)
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.updateUser(update);
        assertEquals(update.getLogin(), controller.getAllUsers().get(controller.getAllUsers().size() - 1).getName());
    }
}