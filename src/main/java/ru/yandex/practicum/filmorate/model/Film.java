package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.LocalDateAfter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Film {
    private int id;

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

    @Builder.Default
    private List<Genre> genres = new ArrayList<>();
}