package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotBlank(message = "Email не может быть пустым.")
    @Email(message = "Некорректный формат email.")
    private String email;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы.")
    private String login;
    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;
}