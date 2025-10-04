package java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private FilmController filmController;
    private FilmStorage filmStorage;
    private FilmService filmService;
    private UserStorage userStorage;

    @BeforeEach
    public void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
    }

    @Test
    public void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class, () -> {
            filmController.addFilm(film);
        });
    }

    @Test
    public void shouldAddFilmWhenDataIsCorrect() {
        Film film = new Film();
        film.setName("Correct Film");
        film.setDescription("Correct Description");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Film addedFilm = filmController.addFilm(film);

        assertNotNull(addedFilm);
        assertEquals(1, addedFilm.getId());
        assertEquals(1, filmController.getAllFilms().size());
    }
}