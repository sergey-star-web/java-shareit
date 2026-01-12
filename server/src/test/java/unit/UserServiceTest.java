package unit;

import org.junit.jupiter.api.BeforeEach;
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
public class UserServiceTest {
    @Autowired
    private UserService userService;

    private UserDto testUser1;
    private UserDto testUser2;
    private Long testUserId1;
    private Long testUserId2;

    @BeforeEach
    public void setup() {
        // Создаем первого тестового пользователя
        User originalUser1 = new User();
        originalUser1.setName("Иван");
        originalUser1.setEmail("ivan@example.com");
        testUser1 = userService.addUser(UserMapper.toUserDto(originalUser1));

        // Создаем второго тестового пользователя
        User originalUser2 = new User();
        originalUser2.setName("Петр");
        originalUser2.setEmail("petr@example.com");
        testUser2 = userService.addUser(UserMapper.toUserDto(originalUser2));

        testUserId1 = testUser1.getId();
        testUserId2 = testUser2.getId();
    }

    @Test
    void testUpdateUser_Success() {
        // Обновляем данные тестового пользователя 1
        UserDto updateDto = new UserDto();
        updateDto.setId(testUserId1);
        updateDto.setName("Иван Иванов");
        updateDto.setEmail("ivanov@example.com");

        UserDto resultDto = userService.updateUser(testUserId1, updateDto);

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

        assertThatThrownBy(() -> userService.updateUser(nonExistingUserId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void testUpdateUser_InvalidEmail_ShouldThrowValidationException() {
        UserDto invalidDto = new UserDto();
        invalidDto.setId(testUserId1);
        invalidDto.setName("Имя");
        invalidDto.setEmail(""); // пустой email

        assertThatThrownBy(() -> userService.updateUser(testUser1.getId(), invalidDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть пустым");
    }

    @Test
    void testDeleteUser_Success() {
        UserDto deletedUserDto = userService.deleteUser(testUserId2);

        assertThat(deletedUserDto).isNotNull();
        assertThat(deletedUserDto.getId()).isEqualTo(testUserId2);
        assertThat(deletedUserDto.getName()).isEqualTo("Петр");
        assertThat(deletedUserDto.getEmail()).isEqualTo("petr@example.com");

        // Проверка, что пользователь реально удален
        UserDto userInRepo = null;
        try {
            userInRepo = userService.getUser(testUserId2);
        } catch (Exception e) {
            userInRepo = null;
        }
        assertThat(userInRepo).isNull();
    }

    @Test
    void testDeleteUser_UserNotFound_ReturnsNull() {
        Long nonExistentUserId = 999L;
        UserDto result = userService.deleteUser(nonExistentUserId);
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