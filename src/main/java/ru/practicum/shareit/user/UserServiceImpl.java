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
    private final String notFoundUserMessage = "Пользователь не найден";
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
        throwIfNoUser(user);
        UserDto userDtoUpdate = setUserFields(user, userDto);
        validateUser(userDtoUpdate);
        User userUpdate = getFullUser(userDtoUpdate);
        repository.update(userId, userUpdate);
        log.info("Пользователь успешно обновлен. Измененный пользователь: {}", userUpdate);
        return userDtoUpdate;
    }

    @Override
    public UserDto deleteUser(Integer userId) {
        User user = repository.delete(userId);
        return UserMapper.toUserDto(user);
    }

    private Integer genNextId() {
        return idCounter++;
    }

    private void throwIfNoUser(User user) {
        if (user == null) {
            log.warn(notFoundUserMessage);
            throw new NotFoundException(notFoundUserMessage);
        }
    }

    private UserDto setUserFields(User userFromRepos, UserDto userDtoUpdate) {
        if (userDtoUpdate.getEmail() != null) {
            userFromRepos.setEmail(userDtoUpdate.getEmail());
        }
        if (userDtoUpdate.getName() != null) {
            userFromRepos.setName(userDtoUpdate.getName());
        }
        return UserMapper.toUserDto(userFromRepos);
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
            log.warn(notFoundUserMessage);
            throw new NotFoundException(notFoundUserMessage);
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