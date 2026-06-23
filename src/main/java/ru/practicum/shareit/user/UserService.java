package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto dto);

    UserDto update(Long id, UserDto dto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    void delete(Long id);
}
