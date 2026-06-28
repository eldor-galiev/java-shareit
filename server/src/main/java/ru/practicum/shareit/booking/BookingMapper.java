package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    private BookingMapper() {
    }

    public static Booking toBooking(BookingCreateDto dto, Item item, User booker) {
        return new Booking(null, dto.getStart(), dto.getEnd(), item, booker, BookingStatus.WAITING);
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookingDto.BookerDto(booking.getBooker().getId(), booking.getBooker().getName()),
                new BookingDto.BookedItemDto(booking.getItem().getId(), booking.getItem().getName())
        );
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(booking.getId(), booking.getBooker().getId());
    }
}
