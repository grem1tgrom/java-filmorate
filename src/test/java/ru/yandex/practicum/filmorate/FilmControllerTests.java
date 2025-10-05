package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTests {
    static Validator validator;
    FilmController controller;
    Film film;

    @BeforeAll
    public static void start() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void beforeEach() {
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), new UserService(new InMemoryUserStorage())));
        film = Film.builder()
                .name("firstName")
                .description("description for FIRST film")
                .releaseDate(LocalDate.of(2000, 7, 18))
                .duration(50)
                .mpa(MPA.builder().id(1).name("G").build())
                .build();
    }

    @Test
    void correctAddFilm() {
        controller.create(film);
        assertEquals(1, film.getId());
    }

    @Test
    void correctAddSeveralFilms() {
        controller.create(film);
        controller.create(film);
        controller.create(film);
        controller.create(film);
        controller.create(film);
        assertEquals(5, controller.getFilms().size());
    }

    @Test
    void getValidationExceptionWhenNameIsNull() {
        film.setName(null);
        Set<ConstraintViolation<Film>> errors = validator.validate(film);
        ConstraintViolation<Film> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Название фильма не может быть пустым", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenBigDescription() {
        film.setDescription("qwertyuiop[]asdfghjkl;'zxcvbnm,.//.,mnbvcxz';lkjhgfdsa][poiuytrewq" +
                "qwertyuiop[]asdfghjkl;'zxcvbnm,.//.,mnbvcxz';lkjhgfdsa][poiuytrewq" +
                "qwertyuiop[]asdfghjkl;'zxcvbnm,.//.,mnbvcxz';lkjhgfdsa][poiuytrewq" +
                "Описание фильма должно быть меньше 200 символов");
        Set<ConstraintViolation<Film>> errors = validator.validate(film);
        ConstraintViolation<Film> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Описание фильма должно быть меньше 200 символов", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenReleaseDateIncorrect() {
        film.setReleaseDate(LocalDate.of(1800, 10, 25));
        Set<ConstraintViolation<Film>> errors = validator.validate(film);
        ConstraintViolation<Film> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Дата выпуска фильма должна быть старше 28.12.1895", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenDuration0() {
        film.setDuration(0);
        Set<ConstraintViolation<Film>> errors = validator.validate(film);
        ConstraintViolation<Film> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Длительность фильма должна быть больше 0", error.getMessage());
    }

    @Test
    void getValidationExceptionWhenDurationLowerZero() {
        film.setDuration(-50);
        Set<ConstraintViolation<Film>> errors = validator.validate(film);
        ConstraintViolation<Film> error = errors.stream().findFirst().orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));
        assertEquals("Длительность фильма должна быть больше 0", error.getMessage());
    }

    @Test
    void correctUpdateFilm() {
        controller.create(film);
        Film update = Film.builder()
                .id(film.getId())
                .description("updated description for tests")
                .name("UPDATING")
                .releaseDate(LocalDate.of(1900, 12, 24))
                .duration(74)
                .mpa(MPA.builder().id(1).name("G").build())
                .build();
        controller.update(update);
        assertEquals(update.getName(), controller.getFilms().get(controller.getFilms().size() - 1).getName());
    }

    @Test
    void getExceptionWhenUpdateFilmAndIdIncorrect() {
        controller.create(film);
        Film update = Film.builder()
                .id(50)
                .description("updated description for tests")
                .name("UPDATING")
                .releaseDate(LocalDate.of(2005, 11, 12))
                .duration(124)
                .mpa(MPA.builder().id(1).name("G").build())
                .build();
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> controller.update(update));
        assertEquals("Фильм с ID - 50 не найден в базе", exception.getMessage());
    }
}