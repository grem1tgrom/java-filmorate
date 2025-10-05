package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        this.userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, this.userService);
    }

    private Film createFilm(String name) {
        Film film = Film.builder()
                .name(name)
                .description("description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MPA(1, "G"))
                .build();
        return filmStorage.addFilm(film);
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.addUser(user);
    }

    @Test
    public void shouldAddLikeToFilm() {
        Film film = createFilm("Film 1");
        User user = createUser("user1");

        filmService.addLike(film.getId(), user.getId());

        assertEquals(1, film.getLikes().size());
        assertTrue(film.getLikes().contains(user.getId()));
    }

    @Test
    public void shouldRemoveLikeFromFilm() {
        Film film = createFilm("Film 1");
        User user = createUser("user1");
        filmService.addLike(film.getId(), user.getId());

        filmService.removeLike(film.getId(), user.getId());

        assertTrue(film.getLikes().isEmpty());
    }

    @Test
    public void shouldThrowValidationExceptionWhenFindTopFilmsCountIsZero() {
        assertThrows(ValidationException.class, () -> {
            filmService.findTopLikedFilms(0);
        });
    }

    @Test
    public void shouldThrowValidationExceptionWhenFindTopFilmsCountIsNegative() {
        assertThrows(ValidationException.class, () -> {
            filmService.findTopLikedFilms(-1);
        });
    }

    @Test
    public void shouldThrowUserNotFoundExceptionWhenAddingLikeWithInvalidUser() {
        Film film = createFilm("Film 1");
        Integer invalidUserId = 9999;

        assertThrows(UserNotFoundException.class, () -> {
            filmService.addLike(film.getId(), invalidUserId);
        });
    }

    @Test
    public void shouldThrowFilmNotFoundExceptionWhenAddingLikeWithInvalidFilm() {
        User user = createUser("validUser");
        Integer invalidFilmId = 9999;

        assertThrows(FilmNotFoundException.class, () -> {
            filmService.addLike(invalidFilmId, user.getId());
        });
    }

    @Test
    public void shouldReturnPopularFilmsSortedByLikes() {
        Film film1 = createFilm("Film 1");
        Film film2 = createFilm("Film 2");
        Film film3 = createFilm("Film 3");

        User user1 = createUser("user1");
        User user2 = createUser("user2");

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        List<Film> popularFilms = filmService.findTopLikedFilms(3);

        assertEquals(3, popularFilms.size());
        assertEquals(film2, popularFilms.get(0));
        assertEquals(film1, popularFilms.get(1));
        assertEquals(film3, popularFilms.get(2));
    }
}