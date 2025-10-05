package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.LocalDateAfter;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class Film {
    private int id;
    @NotEmpty(message = "Название фильма не может быть пустым")
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(max = 200, message = "Описание фильма должно быть меньше 200 символов")
    private String description;
    @LocalDateAfter(value = "28.12.1895", message = "Дата выпуска фильма должна быть старше 28.12.1895")
    private LocalDate releaseDate;
    @Min(value = 1, message = "Длительность фильма должна быть больше 0")
    private int duration;
    @NotNull(message = "Рейтинг фильма не может быть пустым")
    private MPA mpa;
    private List<Genre> genres;
    private Set<Integer> likes;
}