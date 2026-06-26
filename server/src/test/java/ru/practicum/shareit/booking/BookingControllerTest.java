package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;

    private final BookingDto booking = new BookingDto(1L,
            LocalDateTime.of(2026, 6, 26, 12, 0, 0),
            LocalDateTime.of(2026, 6, 27, 12, 0, 0),
            BookingStatus.WAITING,
            new BookingDto.BookerDto(2L, "Booker"),
            new BookingDto.BookedItemDto(3L, "Drill"));

    @Test
    void create() throws Exception {
        when(bookingService.create(eq(2L), any())).thenReturn(booking);
        BookingCreateDto body = new BookingCreateDto(3L,
                LocalDateTime.of(2026, 6, 26, 12, 0, 0), LocalDateTime.of(2026, 6, 27, 12, 0, 0));

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.name").value("Drill"));
    }

    @Test
    void approve() throws Exception {
        when(bookingService.approve(2L, 1L, true)).thenReturn(booking);

        mvc.perform(patch("/bookings/1").header(USER_HEADER, 2).param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(2L, 1L)).thenReturn(booking);

        mvc.perform(get("/bookings/1").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booker.id").value(2));
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(2L, BookingState.ALL)).thenReturn(List.of(booking));

        mvc.perform(get("/bookings").header(USER_HEADER, 2).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(2L, BookingState.ALL)).thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner").header(USER_HEADER, 2).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
