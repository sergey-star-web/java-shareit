package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;

@Component
@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private HashMap<Integer, User> users = new HashMap<>();
    private Integer idCounter = 1;

    private Integer genNextId() {
        return idCounter++;
    }

    @Override
    public User findByUserId(Integer userId) {
        return users.get(userId);
    }

    private void throwIfNoUser(Integer id) {
        if (!users.containsKey(id)) {
            String errorMessage = String.format("Не найден пользователь с %d", id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    public boolean isUserWithEmailExists(String email, Integer userId) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email) && !user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private void validateUser(User user) {
        if (user != null) {
            String email = user.getEmail();
            if (email.isEmpty()) {
                throw new ValidationException("Почта пользователя не может быть пустым");
            } else if (!email.contains("@")) {
                throw new ValidationException("Email должен содержать символ '@'");
            } else if (isUserWithEmailExists(email, user.getId())) {
                throw new ValidationException("Email уже занят другим пользователем");
            }
        } else {
            String errorMessage = "Пользователь не найден";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public User save(User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        user.setId(genNextId());
        validateUser(user);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан. Созданный пользователь: {}", user);
        return user;
    }

    @Override
    public User update(Integer userId, User updateUser) {
        log.info("Получен запрос на обновление пользователя: {}", updateUser);
        updateUser.setId(userId);
        User user = users.get(userId);
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getName() != null) {
            user.setName(updateUser.getName());
        }
        validateUser(user);
        throwIfNoUser(userId);
        users.put(userId, user);
        log.info("Пользователь успешно обновлен. Измененный пользователь: {}", updateUser);
        return updateUser;
    }

    @Override
    public User delete(Integer userId) {
        return users.remove(userId);
    }
}