package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDbStorageTests {
    private final UserDbStorage storage;

    @Test
    @Order(1)
    public void getEmptyUserListWhenUsersEmpty() {
        assertEquals(0, storage.getAllUsers().size());
    }

    @Test
    @Order(2)
    public void correctAddUser() {
        User user = User.builder()
                .email("111@111.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
        User currUser = storage.addUser(user);
        assertEquals(1, currUser.getId());
        assertEquals("testLogin", currUser.getLogin());
    }

    @Test
    @Order(3)
    public void getExceptionWhenIncorrectIdInUpdating() {
        User user = User.builder()
                .email("222@222.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
        storage.addUser(user);
        user.setLogin("newLogin");
        user.setId(987);
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.updateUser(user));
        assertEquals("Пользователь с ID 987 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(4)
    public void getCorrectAllUsersAfterSecondUser() {
        storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        storage.addUser(User.builder().email("2@2.ru").login("u2").name("n2").birthday(LocalDate.now()).build());
        assertEquals(2, storage.getAllUsers().size());
    }

    @Test
    @Order(5)
    public void correctUpdatingUser() {
        User user = User.builder()
                .email("333@333.ru")
                .login("NOT_UPDATED")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
        User addedUser = storage.addUser(user);

        User userToUpdate = User.builder()
                .id(addedUser.getId())
                .email("333@333.ru")
                .login("UPDATED")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();

        storage.updateUser(userToUpdate);
        assertEquals("UPDATED", storage.findUserByID(addedUser.getId()).getLogin());
    }

    @Test
    @Order(6)
    public void getExceptionWhenIncorrectIdInGetUserByID() {
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.findUserByID(741));
        assertEquals("Пользователь с ID 741 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(7)
    public void getCorrectUserByID() {
        User user = User.builder()
                .email("111@111.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
        User addedUser = storage.addUser(user);
        assertEquals("111@111.ru", storage.findUserByID(addedUser.getId()).getEmail());
    }

    @Test
    @Order(8)
    public void getExceptionWhenIncorrectUserIdInFriendship() {
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.addFriendship(338, 1));
        assertEquals("Один из пользователей отсутствует в базе, регистрация дружбы невозможна", exception.getMessage());
    }

    @Test
    @Order(9)
    public void getExceptionWhenIncorrectFriendIdInFriendship() {
        User user = storage.addUser(User.builder().email("2@2.ru").login("u2").name("n2").birthday(LocalDate.now()).build());
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.addFriendship(user.getId(), 222));
        assertEquals("Один из пользователей отсутствует в базе, регистрация дружбы невозможна", exception.getMessage());
    }

    @Test
    @Order(10)
    public void correctAddFriendship() {
        User user1 = storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        User user3 = storage.addUser(User.builder().email("3@3.ru").login("u3").name("n3").birthday(LocalDate.now()).build());
        User user4 = storage.addUser(User.builder().email("4@4.ru").login("u4").name("n4").birthday(LocalDate.now()).build());

        storage.addFriendship(user1.getId(), user3.getId());
        storage.addFriendship(user1.getId(), user4.getId());
        assertEquals(2, storage.getFriendsOfUser(user1.getId()).size());
    }

    @Test
    @Order(11)
    public void getExceptionWhenIncorrectUserInRemovingFriendship() {
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.removeFriendship(147, 1));
        assertEquals("Один из пользователей отсутствует в базе, удаление дружбы невозможно", exception.getMessage());
    }

    @Test
    @Order(12)
    public void getExceptionWhenIncorrectFriendInRemovingFriendship() {
        User user1 = storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        final UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> storage.removeFriendship(user1.getId(), 500));
        assertEquals("Один из пользователей отсутствует в базе, удаление дружбы невозможно", exception.getMessage());
    }

    @Test
    @Order(13)
    public void correctRemovingFriendship() {
        User user1 = storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        User user4 = storage.addUser(User.builder().email("4@4.ru").login("u4").name("n4").birthday(LocalDate.now()).build());
        storage.addFriendship(user1.getId(), user4.getId()); // Сначала добавляем друга
        storage.removeFriendship(user1.getId(), user4.getId());
        assertEquals(0, storage.getFriendsOfUser(user1.getId()).size());
    }

    @Test
    @Order(14)
    public void correctGetEmptyFriendsCrossing() {
        User user1 = storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        User user2 = storage.addUser(User.builder().email("2@2.ru").login("u2").name("n2").birthday(LocalDate.now()).build());
        assertEquals(0, storage.getFriendsCrossing(user1.getId(), user2.getId()).size());
    }

    @Test
    @Order(15)
    public void correctGetFriendsCrossing() {
        User user1 = storage.addUser(User.builder().email("1@1.ru").login("u1").name("n1").birthday(LocalDate.now()).build());
        User user2 = storage.addUser(User.builder().email("2@2.ru").login("u2").name("n2").birthday(LocalDate.now()).build());
        User user3 = storage.addUser(User.builder().email("3@3.ru").login("u3").name("n3").birthday(LocalDate.now()).build());
        storage.addFriendship(user1.getId(), user3.getId());
        storage.addFriendship(user2.getId(), user3.getId());
        List<User> friends = storage.getFriendsCrossing(user1.getId(), user2.getId());
        assertEquals(1, friends.size());
        assertEquals(user3.getId(), friends.get(0).getId());
    }

    @Test
    @Order(16)
    public void getTrueWhenIdPresented() {
        User user4 = storage.addUser(User.builder().email("4@4.ru").login("u4").name("n4").birthday(LocalDate.now()).build());
        assertTrue(storage.idIsPresent(user4.getId()));
    }

    @Test
    @Order(17)
    public void getFalseWhenIdNotPresent() {
        assertFalse(storage.idIsPresent(85858));
    }
}