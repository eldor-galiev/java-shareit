package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createPersistsUser() {
        UserDto created = userService.create(new UserDto(null, "Alice", "alice@mail.com"));

        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findById(created.getId())).isPresent();
    }

    @Test
    void createFailsOnDuplicateEmail() {
        userService.create(new UserDto(null, "Alice", "dup@mail.com"));

        assertThatThrownBy(() -> userService.create(new UserDto(null, "Bob", "dup@mail.com")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void updateChangesOnlyProvidedFields() {
        UserDto created = userService.create(new UserDto(null, "Alice", "alice@mail.com"));

        UserDto updated = userService.update(created.getId(), new UserDto(null, "Alice II", null));

        assertThat(updated.getName()).isEqualTo("Alice II");
        assertThat(updated.getEmail()).isEqualTo("alice@mail.com");
    }

    @Test
    void updateFailsOnDuplicateEmail() {
        userService.create(new UserDto(null, "Alice", "alice@mail.com"));
        UserDto bob = userService.create(new UserDto(null, "Bob", "bob@mail.com"));

        assertThatThrownBy(() -> userService.update(bob.getId(), new UserDto(null, null, "alice@mail.com")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getByIdFailsForUnknownUser() {
        assertThatThrownBy(() -> userService.getById(999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllReturnsCreatedUsers() {
        userService.create(new UserDto(null, "Alice", "alice@mail.com"));
        userService.create(new UserDto(null, "Bob", "bob@mail.com"));

        assertThat(userService.getAll()).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("alice@mail.com", "bob@mail.com");
    }

    @Test
    void deleteRemovesUser() {
        UserDto created = userService.create(new UserDto(null, "Alice", "alice@mail.com"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }
}
