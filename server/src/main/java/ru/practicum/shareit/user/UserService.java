package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUser(Long userId);

    List<UserDto> getUsers();

    UserDto addUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    UserDto deleteUser(Long userId);

    Boolean existsUser(Long userId);

    void validateUser(UserDto userDto);
}