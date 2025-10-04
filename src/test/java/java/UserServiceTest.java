package java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTest {

    private UserService userService;
    private UserStorage userStorage;

    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
    }

    @Test
    public void shouldAddFriend() {
        User user1 = createUser("user1", "user1@mail.com");
        User user2 = createUser("user2", "user2@mail.com");

        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(1, userService.getFriends(user1.getId()).size(), "Список друзей user1 должен содержать одного друга.");
        assertTrue(userService.getFriends(user1.getId()).contains(user2), "В друзьях у user1 должен быть user2.");
        assertEquals(1, userService.getFriends(user2.getId()).size(), "Список друзей user2 должен содержать одного друга.");
        assertTrue(userService.getFriends(user2.getId()).contains(user1), "В друзьях у user2 должен быть user1.");
    }

    @Test
    public void shouldRemoveFriend() {
        User user1 = createUser("user1", "user1@mail.com");
        User user2 = createUser("user2", "user2@mail.com");
        userService.addFriend(user1.getId(), user2.getId());

        userService.removeFriend(user1.getId(), user2.getId());

        assertTrue(userService.getFriends(user1.getId()).isEmpty(), "Список друзей user1 должен быть пуст.");
        assertTrue(userService.getFriends(user2.getId()).isEmpty(), "Список друзей user2 должен быть пуст.");
    }

    @Test
    public void shouldReturnCommonFriends() {
        User user1 = createUser("user1", "user1@mail.com");
        User user2 = createUser("user2", "user2@mail.com");
        User commonFriend = createUser("common", "common@mail.com");

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size(), "Должен быть один общий друг.");
        assertTrue(commonFriends.contains(commonFriend), "Общим другом должен быть 'commonFriend'.");
    }

    @Test
    public void shouldReturnEmptyListIfNoCommonFriends() {
        User user1 = createUser("user1", "user1@mail.com");
        User user2 = createUser("user2", "user2@mail.com");
        User friend1 = createUser("friend1", "friend1@mail.com");
        User friend2 = createUser("friend2", "friend2@mail.com");

        userService.addFriend(user1.getId(), friend1.getId());
        userService.addFriend(user2.getId(), friend2.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty(), "Список общих друзей должен быть пуст.");
    }

    @Test
    public void shouldThrowExceptionWhenAddFriendWithNonExistentUser() {
        User user1 = createUser("user1", "user1@mail.com");
        assertThrows(NotFoundException.class, () -> userService.addFriend(user1.getId(), 9999));
        assertThrows(NotFoundException.class, () -> userService.addFriend(9999, user1.getId()));
    }
}