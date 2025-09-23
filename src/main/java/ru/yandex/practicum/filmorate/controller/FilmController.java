package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов.");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен POST-запрос на добавление фильма: {}", film);
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error("Ошибка валидации: дата релиза не может быть раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен PUT-запрос на обновление фильма: {}", film);
        if (!films.containsKey(film.getId())) {
            log.error("Ошибка обновления: фильм с id={} не найден.", film.getId());
            throw new ValidationException("Фильм с таким id не найден.");
        }
        films.put(film.getId(), film);
        log.info("Фильм успешно обновлен: {}", film);
        return film;
    }
}