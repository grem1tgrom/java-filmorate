package ru.yandex.practicum.filmorate.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LocalDateAfterConstraintValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalDateAfter {
    String value();

    String message() default "Введённая дата старше требуемой";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}