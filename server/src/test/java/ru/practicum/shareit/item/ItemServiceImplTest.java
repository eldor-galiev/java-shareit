package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User newUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemDto itemDto(String name, boolean available) {
        return new ItemDto(null, name, name + " description", available, null, null, null, null);
    }

    @Test
    void createWithoutRequestKeepsRequestIdNull() {
        User owner = newUser("Owner", "owner@mail.com");

        ItemDto created = itemService.create(owner.getId(), itemDto("Drill", true));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getRequestId()).isNull();
    }

    @Test
    void createOnRequestLinksRequest() {
        User requester = newUser("Req", "req@mail.com");
        ItemRequest request = requestRepository.save(
                new ItemRequest(null, "need a drill", requester, LocalDateTime.now()));
        User owner = newUser("Owner", "owner@mail.com");

        ItemDto dto = itemDto("Drill", true);
        dto.setRequestId(request.getId());
        ItemDto created = itemService.create(owner.getId(), dto);

        assertThat(created.getRequestId()).isEqualTo(request.getId());
        Item saved = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void createFailsForUnknownRequest() {
        User owner = newUser("Owner", "owner@mail.com");
        ItemDto dto = itemDto("Drill", true);
        dto.setRequestId(999L);

        assertThatThrownBy(() -> itemService.create(owner.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwnerReturnsOwnerItems() {
        User owner = newUser("Owner", "owner@mail.com");
        itemService.create(owner.getId(), itemDto("Drill", true));
        itemService.create(owner.getId(), itemDto("Saw", true));

        List<ItemDto> items = itemService.getAllByOwner(owner.getId());

        assertThat(items).extracting(ItemDto::getName).containsExactlyInAnyOrder("Drill", "Saw");
    }

    @Test
    void searchReturnsOnlyAvailableMatches() {
        User owner = newUser("Owner", "owner@mail.com");
        itemService.create(owner.getId(), itemDto("Drill", true));
        itemService.create(owner.getId(), itemDto("Drill broken", false));

        assertThat(itemService.search("dRiLl")).extracting(ItemDto::getName).containsExactly("Drill");
        assertThat(itemService.search("")).isEmpty();
    }

    @Test
    void updateChangesOnlyProvidedFields() {
        User owner = newUser("Owner", "owner@mail.com");
        ItemDto created = itemService.create(owner.getId(), itemDto("Drill", true));

        ItemDto patch = new ItemDto(null, "Drill X", null, false, null, null, null, null);
        ItemDto updated = itemService.update(owner.getId(), created.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Drill X");
        assertThat(updated.getAvailable()).isFalse();
        assertThat(updated.getDescription()).isEqualTo("Drill description");
    }

    @Test
    void updateByNonOwnerFails() {
        User owner = newUser("Owner", "owner@mail.com");
        User stranger = newUser("Stranger", "stranger@mail.com");
        ItemDto created = itemService.create(owner.getId(), itemDto("Drill", true));

        assertThatThrownBy(() -> itemService.update(stranger.getId(), created.getId(), itemDto("Hack", true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addCommentRequiresCompletedBooking() {
        User owner = newUser("Owner", "owner@mail.com");
        User booker = newUser("Booker", "booker@mail.com");
        ItemDto item = itemService.create(owner.getId(), itemDto("Drill", true));
        CommentDto comment = new CommentDto(null, "Great tool", null, null);

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), comment))
                .isInstanceOf(ValidationException.class);

        Item itemEntity = itemRepository.findById(item.getId()).orElseThrow();
        User bookerEntity = userRepository.findById(booker.getId()).orElseThrow();
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), itemEntity, bookerEntity, BookingStatus.APPROVED));

        CommentDto saved = itemService.addComment(booker.getId(), item.getId(), comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAuthorName()).isEqualTo("Booker");
    }
}
