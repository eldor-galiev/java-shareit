package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private User newUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item newItem(User owner, boolean available) {
        return itemRepository.save(new Item(null, "Drill", "Powerful", available, owner, null));
    }

    @Test
    void createPersistsWaitingBooking() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        Item item = newItem(owner, true);

        BookingDto booking = bookingService.create(booker.getId(),
                new BookingCreateDto(item.getId(), start, end));

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void createFailsForUnavailableItem() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        Item item = newItem(owner, false);

        assertThatThrownBy(() -> bookingService.create(booker.getId(),
                new BookingCreateDto(item.getId(), start, end)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createFailsWhenOwnerBooksOwnItem() {
        User owner = newUser("Owner", "owner@mail.com");
        Item item = newItem(owner, true);

        assertThatThrownBy(() -> bookingService.create(owner.getId(),
                new BookingCreateDto(item.getId(), start, end)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approveSetsStatus() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        Item item = newItem(owner, true);
        BookingDto created = bookingService.create(booker.getId(),
                new BookingCreateDto(item.getId(), start, end));

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getAllByBookerFiltersByState() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        Item item = newItem(owner, true);
        bookingService.create(booker.getId(), new BookingCreateDto(item.getId(), start, end));

        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.ALL)).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE)).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.PAST)).isEmpty();
    }

    @Test
    void getByIdDeniedForStranger() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        User stranger = newUser("Stranger", "stranger@mail.com");
        Item item = newItem(owner, true);
        BookingDto created = bookingService.create(booker.getId(),
                new BookingCreateDto(item.getId(), start, end));

        assertThat(bookingService.getById(owner.getId(), created.getId()).getId()).isEqualTo(created.getId());
        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), created.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
