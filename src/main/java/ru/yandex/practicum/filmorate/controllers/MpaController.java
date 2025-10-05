package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MPA;
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
    public List<MPA> getAllMpa() {
        log.info("GET запрос на все рейтинги фильмов");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public MPA findMpaByID(@PathVariable int id) {
        log.info("GET запрос на рейтинг фильма с ID {}", id);
        return mpaService.findMpaByID(id);
    }
}