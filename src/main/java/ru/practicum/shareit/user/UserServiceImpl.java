package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto dto) {
        if (userStorage.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already in use: " + dto.getEmail());
        }
        User user = UserMapper.toUser(dto);
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = findUserById(id);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        String newEmail = dto.getEmail();
        if (newEmail != null && !newEmail.isBlank() && newEmail.contains("@")) {
            if (userStorage.existsByEmailAndNotId(newEmail, id)) {
                throw new DuplicateEmailException("Email already in use: " + newEmail);
            }
            existing.setEmail(newEmail);
        }
        return UserMapper.toUserDto(userStorage.update(existing));
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(findUserById(id));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        findUserById(id);
        userStorage.delete(id);
    }

    private User findUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
