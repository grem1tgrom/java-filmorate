package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;

public class FilmValidator {
    public static void validate(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
        }
    }
}