package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto getUser(Integer userId);

    UserDto addUser(UserDto userDto);

    UserDto updateUser(Integer userId, UserDto userDto);

    UserDto deleteUser(Integer userId);
}
