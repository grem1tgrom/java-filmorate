package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService service) {
        this.filmService = service;
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен GET запрос на :PORT/films");
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film create(@Valid @RequestBody final Film film) {
        log.info("Получен POST запрос на :PORT/films");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody final Film film) {
        log.info("Получен PUT запрос на :PORT/films");
        return filmService.updateFilm(film);
    }

    @GetMapping("{id}")
    public Film findFilmByID(@PathVariable Integer id) {
        log.info("Получен GET запрос на :PORT/films/{id}");
        return filmService.findFilmByID(id);
    }

    @PutMapping("/{filmID}/like/{userID}")
    public String addLike(@PathVariable Integer filmID, @PathVariable Integer userID) {
        log.info("Получен PUT запрос на :PORT/films/{film_id}/like/{user_id}");
        return filmService.addLike(filmID, userID);
    }

    @DeleteMapping("/{filmID}/like/{userID}")
    public String removeLike(@PathVariable Integer filmID, @PathVariable Integer userID) {
        log.info("Получен DELETE запрос на :PORT/films/{film_id}/like/{user_id}");
        return filmService.removeLike(filmID, userID);
    }

    @GetMapping("/popular")
    public List<Film> findTopLikedFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен GET запрос на :PORT/films/popular?i=num");
        return filmService.findTopLikedFilms(count);
    }
}