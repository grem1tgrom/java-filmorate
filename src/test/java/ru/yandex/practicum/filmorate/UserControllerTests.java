package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTests {

    private final UserController controller;
    private static Validator validator;
    private User user;

    @BeforeAll
    public static void beforeAll() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .email("123@123.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
    }

    @Test
    void correctSaveUser() {
        User createdUser = controller.createUser(user);
        assertEquals(1, createdUser.getId());
    }

    @Test
    void correctSaveSeveralUsers() {
        controller.createUser(user);
        User user2 = User.builder()
                .email("newUser@mail.com")
                .login("newLogin")
                .name("newName")
                .birthday(LocalDate.now()).build();
        controller.createUser(user2);
        assertEquals(2, controller.getAllUsers().size());
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
        User createdUser = controller.createUser(user);
        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void nameEqualsLoginWhenNameIsBlank() {
        user.setName("         ");
        User createdUser = controller.createUser(user);
        assertEquals("testLogin", createdUser.getName());
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
        User createdUser = controller.createUser(user);
        User update = User.builder()
                .id(createdUser.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name("updatedName")
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.updateUser(update);
        assertEquals(update.getLogin(), controller.getUserByID(createdUser.getId()).getLogin());
    }

    @Test
    void correctUpdateUserWhenNameIsNull() {
        User createdUser = controller.createUser(user);
        User update = User.builder()
                .id(createdUser.getId())
                .email("update@update.com")
                .login("UPDATED")
                .name(null)
                .birthday(LocalDate.of(2001, 1, 24))
                .build();
        controller.updateUser(update);
        assertEquals(update.getLogin(), controller.getUserByID(createdUser.getId()).getName());
    }
}