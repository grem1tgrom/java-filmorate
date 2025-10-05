package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;

public class FilmValidator {
    public static void validate(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
    }
}