package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTests {
    private final GenreDbStorage storage;

    @Test
    public void getCorrectGenreByID() {
        Genre genre = storage.getGenreByID(3);
        assertEquals("Мультфильм", genre.getName());
    }

    @Test
    public void correctGetAllGenres() {
        List<Genre> genres = storage.getAllGenres();
        assertEquals(6, genres.size());
    }

    @Test
    public void getExceptionWhenIncorrectID() {
        final GenreNotFoundException exception = assertThrows(GenreNotFoundException.class, () -> storage.getGenreByID(99));
        assertEquals("Жанр с ID 99 не найден в базе", exception.getMessage());
    }
}