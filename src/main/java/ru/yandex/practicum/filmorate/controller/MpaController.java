package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<Mpa> getAllMpaRatings() {
        log.info("Получен GET-запрос на получение списка всех MPA рейтингов.");
        return mpaService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public Mpa getMpaRatingById(@PathVariable int id) {
        log.info("Получен GET-запрос на получение MPA рейтинга по id={}", id);
        return mpaService.getMpaRatingById(id);
    }
}