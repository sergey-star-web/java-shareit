package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

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

    @Test
    void testDeleteUser_Success() {
        // 1. Создаем тестового пользователя
        User originalUser = new User();
        originalUser.setName("Иван");
        originalUser.setEmail("ivan@example.com");
        UserDto savedUser = userService.addUser(UserMapper.toUserDto(originalUser));
        Long userId = savedUser.getId();

        // 2. Удаляем пользователя
        UserDto deletedUserDto = userService.deleteUser(userId);

        // 3. Проверка, что возвращен DTO удаленного пользователя
        assertThat(deletedUserDto).isNotNull();
        assertThat(deletedUserDto.getId()).isEqualTo(userId);
        assertThat(deletedUserDto.getName()).isEqualTo("Иван");
        assertThat(deletedUserDto.getEmail()).isEqualTo("ivan@example.com");

        // 4. Проверка, что пользователь реально удален из базы
        UserDto userInRepo = userService.getUser(userId);
        assertThat(userInRepo).isNull();
    }

    @Test
    void testDeleteUser_UserNotFound_ReturnsNull() {
        Long nonExistentUserId = 999L; // ID не существующего пользователя
        // 1. Попытка удалить несуществующего пользователя
        UserDto result = userService.deleteUser(nonExistentUserId);
        // 2. Проверка, что результат null
        assertThat(result).isNull();
    }

    @Test
    void testValidateUser_ValidUser_ShouldNotThrow() {
        UserDto user = new UserDto();
        user.setEmail("valid@example.com");
        user.setId(1L);

        assertThatCode(() -> userService.validateUser(user))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidateUser_EmailEmpty_ShouldThrow() {
        UserDto user = new UserDto();
        user.setEmail("");
        user.setId(1L);

        assertThatThrownBy(() -> userService.validateUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Почта пользователя не может быть пустым");
    }

    @Test
    void testValidateUser_InvalidEmail_ShouldThrow() {
        UserDto user = new UserDto();
        user.setEmail("invalidEmail");
        user.setId(1L);

        assertThatThrownBy(() -> userService.validateUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email должен содержать символ '@'");
    }

    @Test
    void testValidateUser_EmailExists_ShouldThrow() {
        UserDto userAdd = new UserDto();
        userAdd.setEmail("taken@example.com");
        userAdd.setName("test");
        userService.addUser(userAdd);

        UserDto user = new UserDto();
        user.setEmail("taken@example.com");
        user.setId(2L);

        assertThatThrownBy(() -> userService.validateUser(user))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email уже занят другим пользователем");
    }

    @Test
    void testValidateUser_NullUser_ShouldThrow() {
        assertThatThrownBy(() -> userService.validateUser(null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }
}
