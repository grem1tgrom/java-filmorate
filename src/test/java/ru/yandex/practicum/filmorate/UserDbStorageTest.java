package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User createNewUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName("Test User " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
    }

    @Test
    public void testCreateAndFindUserById() {
        User newUser = createNewUser("testUser");

        User foundUser = userStorage.getUserById(newUser.getId());

        assertThat(foundUser).isNotNull()
                .hasFieldOrPropertyWithValue("id", newUser.getId())
                .hasFieldOrPropertyWithValue("email", "testUser@mail.com");
    }

    @Test
    public void testUpdateUser() {
        User user = createNewUser("updateTestUser");

        user.setName("Updated Name");
        user.setEmail("updated@mail.com");
        userStorage.updateUser(user);

        User foundUser = userStorage.getUserById(user.getId());

        assertThat(foundUser)
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@mail.com");
    }

    @Test
    public void testGetAllUsers() {
        int initialSize = userStorage.getAllUsers().size();
        createNewUser("userA");
        createNewUser("userB");

        List<User> allUsers = userStorage.getAllUsers();
        assertThat(allUsers).hasSize(initialSize + 2);
    }

    @Test
    public void testAddAndGetFriendsUnilateral() {
        User user1 = createNewUser("friendTest1");
        User user2 = createNewUser("friendTest2");

        userStorage.addFriend(user1.getId(), user2.getId());

        Set<Integer> user1Friends = userStorage.getFriendsIds(user1.getId());
        Set<Integer> user2Friends = userStorage.getFriendsIds(user2.getId());

        assertThat(user1Friends).containsExactly(user2.getId());
        assertThat(user2Friends).isEmpty();
    }

    @Test
    public void testRemoveFriend() {
        User user1 = createNewUser("removeFriend1");
        User user2 = createNewUser("removeFriend2");
        userStorage.addFriend(user1.getId(), user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());
        Set<Integer> user1Friends = userStorage.getFriendsIds(user1.getId());

        assertThat(user1Friends).isEmpty();
    }

    @Test
    public void testGetUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(9999));
    }
}