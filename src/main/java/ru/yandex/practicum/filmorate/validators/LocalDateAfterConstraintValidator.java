package ru.yandex.practicum.filmorate.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAfterConstraintValidator implements ConstraintValidator<LocalDateAfter, LocalDate> {
    private LocalDate annotatedDate;

    @Override
    public void initialize(LocalDateAfter after) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.annotatedDate = LocalDate.parse(after.value(), formatter);
    }

    @Override
    public boolean isValid(LocalDate target, ConstraintValidatorContext cxt) {
        if (target == null) {
            return false;
        }
        return target.isAfter(annotatedDate);
    }
}