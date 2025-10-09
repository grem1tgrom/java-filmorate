package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmControllerTests {

    private final FilmController controller;
    private static Validator validator;
    private Film film;

    @BeforeAll
    public static void start() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void beforeEach() {
        film = Film.builder()
                .name("firstName")
                .description("description for FIRST film")
                .releaseDate(LocalDate.of(2000, 7, 18))
                .duration(50)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
    }

    @Test
    void correctAddFilm() {
        Film createdFilm = controller.createFilm(film);
        assertEquals(1, createdFilm.getId());
    }

    @Test
    void correctAddSeveralFilms() {
        controller.createFilm(film);
        Film film2 = film.toBuilder().name("secondName").build();
        controller.createFilm(film2);
        assertEquals(2, controller.getAllFilms().size());
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
        film.setDescription("q".repeat(201));
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
    void correctUpdateFilm() {
        Film createdFilm = controller.createFilm(film);
        Film update = Film.builder()
                .id(createdFilm.getId())
                .description("updated description for tests")
                .name("UPDATING")
                .releaseDate(LocalDate.of(1900, 12, 24))
                .duration(74)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        controller.updateFilm(update);
        assertEquals(update.getName(), controller.findFilmByID(createdFilm.getId()).getName());
    }

    @Test
    void getExceptionWhenUpdateFilmAndIdIncorrect() {
        controller.createFilm(film);
        Film update = Film.builder()
                .id(50)
                .description("updated description for tests")
                .name("UPDATING")
                .releaseDate(LocalDate.of(2005, 11, 12))
                .duration(124)
                .mpa(MPA.builder().id(1).build())
                .build();
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> controller.updateFilm(update));
        assertEquals("Фильм с ID - 50 не найден в базе", exception.getMessage());
    }
}