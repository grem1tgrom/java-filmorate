package java;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testEmailValidation() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        user.setEmail("not-an-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setEmail("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setEmail(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setEmail("correct@email.com");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testLoginValidation() {
        User user = new User();
        user.setEmail("correct@email.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        user.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setLogin("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setLogin(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setLogin("correct_login");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testBirthdayValidation() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("correct@email.com");

        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        user.setBirthday(LocalDate.now());
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());

        user.setBirthday(LocalDate.now().minusYears(10));
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}