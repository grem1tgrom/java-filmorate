package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmByID(Integer id);

    void deleteFilmByID(Integer id);

    boolean addLike(Integer filmID, Integer userID);

    boolean removeLike(Integer filmID, Integer userID);

    boolean idIsPresent(Integer id);

    List<Film> findTopLikedFilms(Integer count);
}