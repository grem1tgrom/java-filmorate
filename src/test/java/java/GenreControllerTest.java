package java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnAllGenres() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    public void shouldReturnGenreById() throws Exception {
        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    @Test
    public void shouldReturn404ForNonExistentGenre() throws Exception {
        mockMvc.perform(get("/genres/9999"))
                .andExpect(status().isNotFound());
    }
}