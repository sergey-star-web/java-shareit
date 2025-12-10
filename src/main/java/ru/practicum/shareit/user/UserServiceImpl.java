package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private Integer idCounter = 1;

    @Override
    public UserDto getUser(Integer userId) {
        User user = repository.findByUserId(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Получен запрос на создание пользователя: {}", userDto);
        userDto.setId(genNextId());
        validateUser(userDto);
        User userSave = getFullUser(userDto);
        repository.save(userSave);
        log.info("Пользователь успешно создан. Созданный пользователь: {}", userSave);
        return userDto;
    }

    @Override
    public UserDto updateUser(Integer userId, UserDto userDto) {
        log.info("Получен запрос на обновление пользователя: {}", userDto);
        User user = repository.findByUserId(userId);
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        UserDto userDtoFind = UserMapper.toUserDto(user);
        validateUser(userDtoFind);
        throwIfNoUser(userId);
        User userUpdate = getFullUser(userDtoFind);
        repository.update(userId, userUpdate);
        log.info("Пользователь успешно обновлен. Измененный пользователь: {}", userUpdate);
        return userDtoFind;
    }

    @Override
    public UserDto deleteUser(Integer userId) {
        User user = repository.delete(userId);
        return UserMapper.toUserDto(user);
    }

    private Integer genNextId() {
        return idCounter++;
    }

    private void throwIfNoUser(Integer id) {
        if (repository.findByUserId(id) == null) {
            String errorMessage = String.format("Не найден пользователь с %d", id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private void validateUser(UserDto userDto) {
        if (userDto != null) {
            String email = userDto.getEmail();
            if (email.isEmpty()) {
                throw new ValidationException("Почта пользователя не может быть пустым");
            } else if (!email.contains("@")) {
                throw new ValidationException("Email должен содержать символ '@'");
            } else if (isUserWithEmailExists(email, userDto.getId())) {
                throw new ValidationException("Email уже занят другим пользователем");
            }
        } else {
            String errorMessage = "Пользователь не найден";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private boolean isUserWithEmailExists(String email, Integer userId) {
        for (User user : repository.getUsers().values()) {
            if (user.getEmail().equals(email) && !user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private User getFullUser(UserDto userDto) {
        User fullUser = UserMapper.toUser(userDto);
        fullUser.setId(userDto.getId());
        return fullUser;
    }
}