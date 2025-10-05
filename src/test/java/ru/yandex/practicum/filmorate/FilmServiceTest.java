package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @BeforeEach
    public void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return filmStorage.addFilm(film);
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
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
    public void shouldThrowExceptionWhenRemovingNonExistentLike() {
        Film film = createFilm("Film 1");
        User user = createUser("user1");

        assertThrows(NotFoundException.class, () -> filmService.removeLike(film.getId(), user.getId()));
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

        List<Film> popularFilms = filmService.getPopularFilms(3);

        assertEquals(3, popularFilms.size());
        assertEquals(film2, popularFilms.get(0));
        assertEquals(film1, popularFilms.get(1));
        assertEquals(film3, popularFilms.get(2));
    }
}