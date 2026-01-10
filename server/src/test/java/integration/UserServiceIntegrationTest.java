package integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.*;

import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.booking.*;
import config.AppConfig;
import config.PersistenceConfig;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Rollback
@SpringBootTest(classes = ShareItApp.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = { "jdbc.url=jdbc:postgresql://localhost:5432/test"})
@SpringJUnitConfig({UserServiceImpl.class, BookingServiceImpl.class, ItemServiceImpl.class, AppConfig.class,
        PersistenceConfig.class, UserRepository.class, BookingRepository.class, ItemRepository.class,})
public class UserServiceIntegrationTest {
    private final UserService userService;

    @Test
    void testUpdateUser_Success() {
        // 1. Создаем тестового пользователя
        User originalUser = new User();
        originalUser.setName("Иван");
        originalUser.setEmail("ivan@example.com");
        // сохранить пользователя (предположим, есть метод)
        UserDto savedUser = userService.addUser(UserMapper.toUserDto(originalUser));
        Long userId = savedUser.getId();

        // 2. Создаем DTO с обновленными данными
        UserDto updateDto = new UserDto();
        updateDto.setId(userId);
        updateDto.setName("Иван Иванов");
        updateDto.setEmail("ivanov@example.com");

        // 3. Обновляем пользователя
        UserDto resultDto = userService.updateUser(userId, updateDto);

        // 4. Проверка, что изменение прошло успешно
        assertThat(resultDto.getName()).isEqualTo("Иван Иванов");
        assertThat(resultDto.getEmail()).isEqualTo("ivanov@example.com");
    }

    @Test
    void testUpdateUser_UserNotFound() {
        Long nonExistingUserId = 999L;

        UserDto updateDto = new UserDto();
        updateDto.setId(nonExistingUserId);
        updateDto.setName("Неизвестный");
        updateDto.setEmail("unknown@example.com");

        // Ожидаем исключение NotFoundException
        assertThatThrownBy(() -> userService.updateUser(nonExistingUserId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден"); // или ваше сообщение
    }

    @Test
    void testUpdateUser_InvalidEmail_ShouldThrowValidationException() {
        User originalUser = new User();
        originalUser.setName("Иван");
        originalUser.setEmail("ivan@example.com");
        // Создаем тестового пользователя
        UserDto savedUser = userService.addUser(UserMapper.toUserDto(originalUser));
        Long userId = savedUser.getId();

        UserDto invalidDto = new UserDto();
        invalidDto.setId(userId);
        invalidDto.setName("Имя");
        invalidDto.setEmail(""); // пустой email

        // Проверка, что выбрасывается ValidationException
        assertThatThrownBy(() -> userService.updateUser(userId, invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть пустым");
    }
}
