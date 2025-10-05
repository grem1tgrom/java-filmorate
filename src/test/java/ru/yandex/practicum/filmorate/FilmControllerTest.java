package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();

        this.userService = new UserService(userStorage);
        this.filmService = new FilmService(filmStorage, this.userService);

        this.filmController = new FilmController(filmService);
    }

    private Film getValidFilm() {
        return Film.builder()
                .name("Valid Movie")
                .description("A great comedy")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MPA(1, "G"))
                .build();
    }

    @Test
    public void shouldCreateFilmWhenDataIsCorrect() {
        Film film = getValidFilm();

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    public void shouldThrowValidationExceptionWhenReleaseDateIsTooEarly() {
        Film film = getValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldThrowValidationExceptionWhenDurationIsNegative() {
        Film film = getValidFilm();
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldReturnAllFilms() {
        filmController.create(getValidFilm());

        Film film2 = getValidFilm();
        film2.setName("Second Movie");
        filmController.create(film2);

        List<Film> allFilms = filmController.getFilms();

        assertEquals(2, allFilms.size());
    }
}