package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private static final String NOT_FOUND_USER_MESSAGE = "Пользователь не найден";

    @Override
    public UserDto getUser(Long userId) {
        return repository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElse(null);
    }

    @Override
    public List<UserDto> getUsers() {
        List<User> users = repository.findAll();
        return UserMapper.toUsersDto(users);
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        log.info("Получен запрос на создание пользователя: {}", userDto);
        validateUser(userDto);
        User userSave = UserMapper.toUser(userDto);
        Long id = repository.save(userSave).getId();
        userDto.setId(id);
        log.info("Пользователь успешно создан. Созданный пользователь: {}", userDto);
        return userDto;
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Получен запрос на обновление пользователя: {}", userDto);
        User user = repository.findById(userId).orElse(null);
        if (user == null) {
            log.warn(NOT_FOUND_USER_MESSAGE);
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        }
        UserDto userDtoUpdate = setUserFields(user, userDto);
        validateUser(userDtoUpdate);
        User userUpdate = getFullUser(userDtoUpdate);
        repository.save(userUpdate);
        log.info("Пользователь успешно обновлен. Измененный пользователь: {}", userUpdate);
        return userDtoUpdate;
    }

    @Override
    @Transactional
    public UserDto deleteUser(Long userId) {
        User user = repository.findById(userId).orElse(null);
        if (user != null) {
            repository.delete(user);
            return UserMapper.toUserDto(user);
        }
        return null;
    }

    @Override
    public Boolean existsUser(Long userId) {
        return repository.existsById(userId);
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
            log.warn(NOT_FOUND_USER_MESSAGE);
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        }
    }

    private boolean isUserWithEmailExists(String email, Long userId) {
        for (User user : repository.findAll()) {
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