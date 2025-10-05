package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTests {
    private final MpaDbStorage storage;

    @Test
    public void getCorrectMpaByID() {
        MPA mpa = storage.getMpaByID(4);
        assertEquals("R", mpa.getName());
    }

    @Test
    public void correctGetAllMPA() {
        List<MPA> allMPA = storage.getAllMpa();
        assertEquals(5, allMPA.size());
    }

    @Test
    public void getExceptionWhenIncorrectID() {
        final MpaNotFoundException exception = assertThrows(MpaNotFoundException.class, () -> storage.getMpaByID(85));
        assertEquals("Рейтинг с ID 85 не найден в базе", exception.getMessage());
    }
}