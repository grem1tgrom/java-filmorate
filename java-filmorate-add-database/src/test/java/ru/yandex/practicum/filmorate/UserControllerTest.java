package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest {

    private UserController userController;
    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    @Test
    public void shouldUseLoginWhenNameIsEmpty() {
        User user = new User();
        user.setLogin("myLogin");
        user.setEmail("test@test.com");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        user.setName("");

        User createdUser = userController.createUser(user);

        assertEquals("myLogin", createdUser.getName());
    }

    @Test
    public void shouldCreateUserWhenDataIsCorrect() {
        User user = new User();
        user.setLogin("correctUser");
        user.setName("Correct Name");
        user.setEmail("correct@email.com");
        user.setBirthday(LocalDate.of(1995, 10, 20));

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser);
        assertEquals(1, createdUser.getId());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    public void shouldSetLoginAsNameWhenNameIsMissing() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("test@email.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName(null);

        User createdUser = userController.createUser(user);

        assertEquals("login", createdUser.getName());
    }
}