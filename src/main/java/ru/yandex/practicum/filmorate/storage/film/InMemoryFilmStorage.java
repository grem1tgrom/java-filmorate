package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component("FilmRamStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    protected int nextID = 1;
    protected Map<Integer, Film> films = new HashMap<>();

    @Override
    public List<Film> getAllFilms() {
        log.debug("Текущее количество фильмов в базе: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(nextID);
        films.put(film.getId(), film);
        nextID++;
        log.debug("Добавлен фильм: {}; его ID: {}; всего фильмов в базе: {}", film.getName(), film.getId(), films.size());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new FilmNotFoundException("Фильм с ID - " + film.getId() + " не найден в базе");
        }
        films.put(film.getId(), film);
        log.debug("Обновлен фильм: {}; его ID: {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film getFilmByID(Integer id) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException("Фильм с ID - " + id + " не найден в базе");
        }
        log.debug("Фильм с ID {} запрошен из базы", id);
        return films.get(id);
    }

    @Override
    public void deleteFilmByID(Integer id) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException("Фильм с ID - " + id + " не найден в базе");
        }
        films.remove(id);
        log.debug("Фильм с ID {} удалён из базы, в базе осталось {} фильмов", id, films.size());
    }

    @Override
    public boolean addLike(Integer filmID, Integer userID) {
        if (!films.containsKey(filmID)) {
            throw new FilmNotFoundException("Фильм с ID - " + filmID + " не найден в базе");
        }
        Set<Integer> likes = films.get(filmID).getLikes();
        boolean isAdded = likes.add(userID);
        if (isAdded) {
            log.debug("Добавлен лайк к фильму с ID {} от пользователя с ID {}, всего лайков {}", filmID, userID, likes.size());
            films.get(filmID).setLikes(likes);
        } else {
            log.debug("Лайк от пользователя с ID {} уже есть у фильма с ID {}", userID, filmID);
        }
        return isAdded;
    }

    @Override
    public boolean removeLike(Integer filmID, Integer userID) {
        if (!films.containsKey(filmID)) {
            throw new FilmNotFoundException("Фильм с ID - " + filmID + " не найден в базе");
        }
        Set<Integer> likes = films.get(filmID).getLikes();
        boolean isRemoved = likes.remove(userID);
        if (isRemoved) {
            log.debug("Лайк к фильму с ID {} от пользователя с ID {} удален, всего лайков {}", filmID, userID, likes.size());
            films.get(filmID).setLikes(likes);
        } else {
            log.debug("Лайк от пользователя с ID {} отсутствует у фильма с ID {}", userID, filmID);
        }
        return isRemoved;
    }

    @Override
    public List<Film> findTopLikedFilms(Integer count) {
        if (films.size() <= count) {
            log.debug("Запрошенное количество фильмов превышает базу фильмов, итоговый размер топ списка {}", films.size());
            return films.values().stream().sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                    .collect(Collectors.toList());
        }
        return films.values().stream().sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                .limit(count).collect(Collectors.toList());
    }

    @Override
    public boolean idIsPresent(Integer id) {
        log.debug("Запрошена проверка ID {} в базе пользователей", id);
        return films.containsKey(id);
    }
}