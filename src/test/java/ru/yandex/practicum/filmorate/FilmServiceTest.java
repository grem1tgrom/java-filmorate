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
        filmService = new FilmService(filmStorage);
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

        assertEquals(1, film.getLikes().size(), "У фильма должен быть один лайк.");
        assertTrue(film.getLikes().contains(user.getId()), "Фильм должен содержать лайк от user1.");
    }

    @Test
    public void shouldRemoveLikeFromFilm() {
        Film film = createFilm("Film 1");
        User user = createUser("user1");
        filmService.addLike(film.getId(), user.getId());

        filmService.removeLike(film.getId(), user.getId());

        assertTrue(film.getLikes().isEmpty(), "У фильма не должно быть лайков.");
    }

    @Test
    public void shouldThrowExceptionWhenRemovingNonExistentLike() {
        Film film = createFilm("Film 1");
        User user = createUser("user1");

        assertThrows(NotFoundException.class, () -> {
            filmService.removeLike(film.getId(), user.getId());
        }, "Должно быть выброшено исключение NotFoundException.");
    }

    @Test
    public void shouldReturnPopularFilmsSortedByLikes() {
        Film film1 = createFilm("Film 1"); // 1 лайк
        Film film2 = createFilm("Film 2"); // 2 лайка
        Film film3 = createFilm("Film 3"); // 0 лайков

        User user1 = createUser("user1");
        User user2 = createUser("user2");

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        List<Film> popularFilms = filmService.getPopularFilms(3);

        assertEquals(3, popularFilms.size(), "Список должен содержать 3 фильма.");
        assertEquals(film2, popularFilms.get(0), "Самым популярным должен быть film2.");
        assertEquals(film1, popularFilms.get(1), "Вторым по популярности должен быть film1.");
        assertEquals(film3, popularFilms.get(2), "Третьим должен быть film3.");
    }
}