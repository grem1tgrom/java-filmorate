package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    private int id;
    @NotBlank
    @Email(message = "Передана некорректная почта")
    private String email;
    @NotEmpty(message = "Логин пользователя не может быть пустым")
    private String login;
    private String name;
    @PastOrPresent(message = "Введена дата рождения в будущем")
    private LocalDate birthday;
    private Set<Integer> friends;
}