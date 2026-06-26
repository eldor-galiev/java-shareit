package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void parsesDatesInExpectedFormat() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-06-26T12:00:00\",\"end\":\"2026-06-27T12:00:00\"}";

        BookingCreateDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 6, 26, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 6, 27, 12, 0, 0));
    }

    @Test
    void serializesStartInExpectedFormat() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(1L,
                LocalDateTime.of(2026, 6, 26, 12, 0, 0), LocalDateTime.of(2026, 6, 27, 12, 0, 0));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-06-26T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-06-27T12:00:00");
    }
}
