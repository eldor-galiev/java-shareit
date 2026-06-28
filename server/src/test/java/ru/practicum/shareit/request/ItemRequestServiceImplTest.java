package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User newUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemRequestDto dto(String description) {
        return new ItemRequestDto(null, description, null, null);
    }

    @Test
    void createPersistsRequestWithEmptyAnswers() {
        User user = newUser("Alice", "alice@mail.com");

        ItemRequestDto created = requestService.create(user.getId(), dto("Need a drill"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
        assertThat(requestRepository.findById(created.getId())).isPresent();
    }

    @Test
    void createFailsForUnknownUser() {
        assertThatThrownBy(() -> requestService.create(999L, dto("x")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnReturnsNewestFirstWithAnswers() {
        User owner = newUser("Owner", "owner@mail.com");
        User responder = newUser("Bob", "bob@mail.com");
        ItemRequestDto first = requestService.create(owner.getId(), dto("first"));
        requestService.create(owner.getId(), dto("second"));
        attachItem(responder, first.getId(), "Drill");

        List<ItemRequestDto> own = requestService.getOwn(owner.getId());

        assertThat(own).hasSize(2);
        assertThat(own).extracting(ItemRequestDto::getCreated)
                .isSortedAccordingTo(Comparator.reverseOrder());
        ItemRequestDto withItems = own.stream()
                .filter(r -> r.getId().equals(first.getId())).findFirst().orElseThrow();
        assertThat(withItems.getItems()).singleElement()
                .satisfies(item -> {
                    assertThat(item.getName()).isEqualTo("Drill");
                    assertThat(item.getOwnerId()).isEqualTo(responder.getId());
                });
    }

    @Test
    void getAllReturnsOnlyOtherUsersRequests() {
        User owner = newUser("Owner", "owner@mail.com");
        User other = newUser("Other", "other@mail.com");
        requestService.create(owner.getId(), dto("mine"));
        ItemRequestDto foreign = requestService.create(other.getId(), dto("foreign"));

        List<ItemRequestDto> all = requestService.getAll(owner.getId());

        assertThat(all).extracting(ItemRequestDto::getId).containsExactly(foreign.getId());
    }

    @Test
    void getByIdReturnsRequestWithAnswers() {
        User owner = newUser("Owner", "owner@mail.com");
        User responder = newUser("Bob", "bob@mail.com");
        ItemRequestDto request = requestService.create(owner.getId(), dto("Need a ladder"));
        attachItem(responder, request.getId(), "Ladder");

        ItemRequestDto found = requestService.getById(responder.getId(), request.getId());

        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getItems()).extracting("name").containsExactly("Ladder");
    }

    @Test
    void getByIdFailsForUnknownRequest() {
        User user = newUser("Alice", "alice@mail.com");
        assertThatThrownBy(() -> requestService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    private void attachItem(User owner, Long requestId, String name) {
        ItemRequest request = requestRepository.findById(requestId).orElseThrow();
        itemRepository.save(new Item(null, name, name + " description", true, owner, request));
    }
}
