package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService requestService;

    private final ItemRequestDto response = new ItemRequestDto(1L, "need a drill",
            LocalDateTime.of(2026, 6, 26, 12, 30, 0),
            List.of(new ItemForRequestDto(2L, "Drill", 3L)));

    @Test
    void create() throws Exception {
        when(requestService.create(eq(1L), any())).thenReturn(response);

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemRequestDto(null, "need a drill", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("need a drill"))
                .andExpect(jsonPath("$.created").value("2026-06-26T12:30:00"));
    }

    @Test
    void getOwn() throws Exception {
        when(requestService.getOwn(1L)).thenReturn(List.of(response));

        mvc.perform(get("/requests").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"));
    }

    @Test
    void getAll() throws Exception {
        when(requestService.getAll(1L)).thenReturn(List.of(response));

        mvc.perform(get("/requests/all").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getById() throws Exception {
        when(requestService.getById(1L, 1L)).thenReturn(response);

        mvc.perform(get("/requests/1").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].ownerId").value(3));
    }
}
