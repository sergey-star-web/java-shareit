package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser; // поле для хранения тестового пользователя
    private Long testUserId; // поле для хранения ID

    @BeforeEach
    public void setUp() {
        // Создаем и сохраняем пользователя перед каждым тестом
        User user = new User();
        user.setName("Организационный тест");
        user.setEmail("testsetup@example.com");
        testUser = userRepository.save(user);
        testUserId = testUser.getId();
    }

    @Test
    public void testSaveAndFindById() {
        // Теперь не нужно создавать пользователя вручную, используем testUser
        Optional<User> foundUserOpt = userRepository.findById(testUserId);
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getName()).isEqualTo("Организационный тест");
        assertThat(foundUser.getEmail()).isEqualTo("testsetup@example.com");
    }

    @Test
    public void testFindAll() {
        // Создаем еще одного пользователя, чтобы проверить findAll
        User user2 = new User();
        user2.setName("Пользователь2");
        user2.setEmail("user2@example.com");
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
        assertThat(users).extracting(User::getEmail).containsAnyOf("testsetup@example.com", "user2@example.com");
    }

    @Test
    public void testExistsById() {
        // Проверяем, что наш тестовый пользователь существует
        boolean exists = userRepository.existsById(testUserId);
        assertThat(exists).isTrue();

        // Удаляем пользователя
        userRepository.deleteById(testUserId);

        // Проверяем, что его больше нет
        boolean existsAfterDelete = userRepository.existsById(testUserId);
        assertThat(existsAfterDelete).isFalse();
    }
}