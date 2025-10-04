package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> getAllGenres();
    Optional<Genre> getGenreById(int id);
    List<Genre> getFilmGenres(int filmId);
    void updateFilmGenres(int filmId, List<Genre> genres);
}