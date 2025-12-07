package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

public interface UserRepository {
    User findByUserId(Integer userId);

    User save(User user);

    User update(Integer userId, User user);

    User delete(Integer userId);
}