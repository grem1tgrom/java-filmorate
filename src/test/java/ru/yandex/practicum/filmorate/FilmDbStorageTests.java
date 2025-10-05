package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmDbStorageTests {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    @Order(1)
    public void getEmptyFilmsWhenWeDontHaveFilms() {
        assertEquals(0, filmStorage.getAllFilms().size());
    }

    @Test
    @Order(2)
    public void correctAddFilm() {
        Film film = Film.builder()
                .name("firstFilmName")
                .description("description for FIRST film")
                .releaseDate(LocalDate.of(2000, 7, 18))
                .duration(110)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        Film createdFilm = filmStorage.addFilm(film);
        assertEquals(1, createdFilm.getId());
        assertEquals("firstFilmName", createdFilm.getName());
    }

    @Test
    @Order(3)
    public void getExceptionWhenIncorrectIdInUpdating() {
        Film film = Film.builder()
                .id(158)
                .name("UpdatedFilmName")
                .description("UpdatedDescription")
                .releaseDate(LocalDate.of(2000, 7, 18))
                .duration(110)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmStorage.updateFilm(film));
        assertEquals("Фильм с ID - 158 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(4)
    public void correctFilmUpdating() {
        Film film = Film.builder()
                .id(1)
                .name("UpdatedFilmName")
                .description("UpdatedDescription")
                .releaseDate(LocalDate.of(2000, 7, 18))
                .duration(110)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        Film updatedFilm = filmStorage.updateFilm(film);
        assertEquals(1, updatedFilm.getId());
        assertEquals("UpdatedFilmName", updatedFilm.getName());
        assertEquals("UpdatedDescription", updatedFilm.getDescription());
    }

    @Test
    @Order(5)
    public void correctGettingAllFilmsAfterSeveralNewFilms() {
        Film secondFilm = Film.builder()
                .name("SecondFilm")
                .description("SecondFilmDescription")
                .releaseDate(LocalDate.of(2020, 4, 25))
                .duration(90)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        Film thirdFilm = Film.builder()
                .name("ThirdFilm")
                .description("ThirdFilmDescription")
                .releaseDate(LocalDate.of(2015, 1, 19))
                .duration(145)
                .mpa(MPA.builder().id(1).name("G").build())
                .genres(new ArrayList<>())
                .build();
        filmStorage.addFilm(secondFilm);
        filmStorage.addFilm(thirdFilm);
        assertEquals(3, filmStorage.getAllFilms().size());
    }

    @Test
    @Order(6)
    public void getExceptionWhenIncorrectIdInGettingById() {
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmStorage.getFilmByID(852));
        assertEquals("Фильм с ID - 852 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(7)
    public void correctGettingFilmByID() {
        Film film = filmStorage.getFilmByID(2);
        assertEquals("SecondFilm", film.getName());
        assertEquals("SecondFilmDescription", film.getDescription());
    }

    @Test
    @Order(8)
    public void getTopWhenDontHaveLikes() {
        List<Film> top = filmStorage.findTopLikedFilms(10);
        assertEquals(3, top.size());
    }

    @Test
    @Order(9)
    public void getExceptionWhenIncorrectIdInAddingLike() {
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmStorage.addLike(50, 1));
        assertEquals("Фильм с ID - 50 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(10)
    public void correctLikeAdding() {
        User user = User.builder()
                .email("222@222.ru")
                .login("testLogin")
                .name("TestingName")
                .birthday(LocalDate.of(2002, 2, 23))
                .build();
        userStorage.addUser(user);
        boolean added = filmStorage.addLike(3, 1);
        assertTrue(added);
    }

    @Test
    @Order(11)
    public void getFalseIfAddSameLike() {
        boolean added = filmStorage.addLike(3, 1);
        assertFalse(added);
    }

    @Test
    @Order(12)
    public void getCorrectTopFilmsAfterLikes() {
        List<Film> top = filmStorage.findTopLikedFilms(10);
        assertEquals(3, top.size());
        assertEquals("ThirdFilm", top.get(0).getName());
    }

    @Test
    @Order(13)
    public void correctDeletingLike() {
        boolean deleted = filmStorage.removeLike(3, 1);
        assertTrue(deleted);
    }

    @Test
    @Order(14)
    public void getFalseWhenTryDeleteAlreadyDeletedLike() {
        boolean deleted = filmStorage.removeLike(3, 1);
        assertFalse(deleted);
    }

    @Test
    @Order(15)
    public void getExceptionWhenIncorrectFilmIdInDeletingLike() {
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> filmStorage.removeLike(147, 1));
        assertEquals("Фильм с ID - 147 не найден в базе", exception.getMessage());
    }

    @Test
    @Order(16)
    public void getCorrectTopFilmsAfterRemovingLikes() {
        List<Film> top = filmStorage.findTopLikedFilms(10);
        assertEquals(3, top.size());
        assertEquals("UpdatedFilmName", top.get(0).getName());
    }

    @Test
    @Order(17)
    public void getFalseWhenIdIsNotPresentInBase() {
        assertFalse(filmStorage.idIsPresent(567));
    }

    @Test
    @Order(18)
    public void getTrueWhenIdIsPresentInBase() {
        assertTrue(filmStorage.idIsPresent(2));
    }
}