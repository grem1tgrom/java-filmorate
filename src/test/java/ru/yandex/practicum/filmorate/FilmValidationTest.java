package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        return film;
    }

    @Test
    public void testNameValidation() {
        Film film = createValidFilm();

        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        film.setName("");
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        film.setName(" ");
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDescriptionValidation() {
        Film film = createValidFilm();
        String longDescription = "a".repeat(201);
        film.setDescription(longDescription);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        film.setDescription("a".repeat(200));
        violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testReleaseDateValidation() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDurationValidation() {
        Film film = createValidFilm();

        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        film.setDuration(-1);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testMpaValidation() {
        Film film = createValidFilm();
        film.setMpa(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}