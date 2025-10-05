package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class Film {
    private int id;
    @NotBlank(message = "Название не может быть пустым.")
    private String name;
    @Size(min = 1, max = 200, message = "Описание не должно быть пустым и не должно превышать 200 символов.")
    private String description;
    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private int duration;
    private MPA mpa;
    private List<Genre> genres;
    private final Set<Integer> likes = new HashSet<>();
}