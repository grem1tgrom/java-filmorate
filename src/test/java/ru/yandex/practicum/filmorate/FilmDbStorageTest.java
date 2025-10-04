package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film createNewFilm(String name, int mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(mpaId, null));

        film.getGenres().add(new Genre(1, null));
        film.getGenres().add(new Genre(2, null));

        return filmStorage.addFilm(film);
    }

    private User createNewUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
    }

    @Test
    public void testCreateAndFindFilmById() {
        Film newFilm = createNewFilm("TestFilm", 3);

        Optional<Film> filmOptional = filmStorage.getFilmById(newFilm.getId());
        assertThat(filmOptional).isPresent();
        Film foundFilm = filmOptional.get();

        assertThat(foundFilm).isNotNull()
                .hasFieldOrPropertyWithValue("name", "TestFilm")
                .extracting("mpa").extracting("id").isEqualTo(3);

        List<Integer> genreIds = foundFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toList());
        assertThat(genreIds).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void testUpdateFilm() {
        Film film = createNewFilm("UpdateTestFilm", 1);

        film.setName("Updated Film Name");
        film.setDuration(150);
        film.setMpa(new Mpa(5, null));

        film.getGenres().clear();
        film.getGenres().add(new Genre(3, null));

        Film updatedFilm = filmStorage.updateFilm(film);

        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("name", "Updated Film Name")
                .extracting("mpa").extracting("id").isEqualTo(5);

        List<Integer> genreIds = updatedFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toList());
        assertThat(genreIds).containsExactly(3);
    }

    @Test
    public void testAddAndRemoveLike() {
        Film film = createNewFilm("LikeTestFilm", 1);
        User user = createNewUser("liker");

        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> filmAfterLikeOptional = filmStorage.getFilmById(film.getId());
        assertThat(filmAfterLikeOptional).isPresent();
        Film filmAfterLike = filmAfterLikeOptional.get();
        assertThat(filmAfterLike.getLikes()).containsExactly(user.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        Optional<Film> filmAfterUnlikeOptional = filmStorage.getFilmById(film.getId());
        assertThat(filmAfterUnlikeOptional).isPresent();
        Film filmAfterUnlike = filmAfterUnlikeOptional.get();
        assertThat(filmAfterUnlike.getLikes()).isEmpty();

        assertThrows(NotFoundException.class, () -> filmStorage.removeLike(film.getId(), user.getId()));
    }
}