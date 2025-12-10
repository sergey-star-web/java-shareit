package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserRepository {
    Map<Integer, User> getUsers();

    User findByUserId(Integer userId);

    User save(User user);

    User update(Integer userId, User user);

    User delete(Integer userId);
}