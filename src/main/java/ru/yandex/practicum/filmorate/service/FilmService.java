package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        FilmValidator.validate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.idIsPresent(film.getId())) {
            throw new FilmNotFoundException("Фильм с ID - " + film.getId() + " не найден в базе");
        }
        FilmValidator.validate(film);
        return filmStorage.updateFilm(film);
    }

    public Film findFilmByID(Integer id) {
        if (!filmStorage.idIsPresent(id)) {
            throw new FilmNotFoundException("Фильм с ID - " + id + " не найден в базе");
        }
        return filmStorage.getFilmByID(id);
    }

    public void addLike(Integer filmID, Integer userID) {
        if (!filmStorage.idIsPresent(filmID)) {
            throw new FilmNotFoundException("Фильм с ID " + filmID + " не найден в базе");
        }
        if (!userService.checkUserIdInStorage(userID)) {
            throw new UserNotFoundException("Пользователь с ID - " + userID + " не найден в базе");
        }
        filmStorage.addLike(filmID, userID);
    }

    public void removeLike(Integer filmID, Integer userID) {
        if (!filmStorage.idIsPresent(filmID)) {
            throw new FilmNotFoundException("Фильм с ID " + filmID + " не найден в базе");
        }
        if (!userService.checkUserIdInStorage(userID)) {
            throw new UserNotFoundException("Пользователь с ID - " + userID + " не найден в базе");
        }
        filmStorage.removeLike(filmID, userID);
    }

    public List<Film> findTopLikedFilms(Integer count) {
        if (count == null || count <= 0) {
            throw new ValidationException("Количество выводимых фильмов должно быть больше 0");
        }
        log.info("Запрошен топ фильмов размерностью {}", count);
        return filmStorage.findTopLikedFilms(count);
    }
}