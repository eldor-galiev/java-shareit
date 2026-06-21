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
        User existing = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        if (dto.getEmail() != null && userStorage.existsByEmailAndNotId(dto.getEmail(), id)) {
            throw new DuplicateEmailException("Email already in use: " + dto.getEmail());
        }
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.update(existing));
    }

    @Override
    public UserDto getById(Long id) {
        return userStorage.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id);
    }
}
