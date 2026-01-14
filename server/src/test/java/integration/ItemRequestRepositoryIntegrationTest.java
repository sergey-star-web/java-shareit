package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class ItemRequestRepositoryIntegrationTest {
    private static final LocalDate NOW = LocalDate.now();
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    private Long userId1;
    private Long userId2;
    private Long userId3;

    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;
    private ItemRequest request4;

    @BeforeEach
    void setUp() {
        // Создаём и сохраняем 3 пользователя
        user1 = new User();
        user1.setName("Пользователь 1");
        user1.setEmail("user1@example.com");
        user1.setId(userRepository.save(user1).getId());
        userId1 = user1.getId();

        user2 = new User();
        user2.setName("Пользователь 2");
        user2.setEmail("user2@example.com");
        user2.setId(userRepository.save(user2).getId());
        userId2 = user2.getId();

        user3 = new User();
        user3.setName("Пользователь 3");
        user3.setEmail("user3@example.com");
        user3.setId(userRepository.save(user3).getId());
        userId3 = user3.getId();

        // Создаём и сохраняем запросы
        request1 = new ItemRequest();
        request1.setRequestorId(userId1);
        request1.setCreated(NOW.minusDays(2));
        itemRequestRepository.save(request1);

        request2 = new ItemRequest();
        request2.setRequestorId(userId1);
        request2.setCreated(NOW.minusDays(1));
        itemRequestRepository.save(request2);

        request3 = new ItemRequest();
        request3.setRequestorId(userId2);
        request3.setCreated(NOW.minusDays(3));
        itemRequestRepository.save(request3);

        request4 = new ItemRequest();
        request4.setRequestorId(userId3);
        request4.setCreated(NOW.minusDays(5));
        itemRequestRepository.save(request4);
    }

    @Test
    void testFindAllByRequestorIdOrderByCreatedDesc() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId1);

        assertThat(requests).isNotEmpty();
        assertThat(requests.size()).isGreaterThanOrEqualTo(2);
        // Проверка порядка: самый последний (по времени) - первый
        assertThat(requests.get(0).getCreated()).isAfterOrEqualTo(requests.get(1).getCreated());
        // Дополнительно можно проверить, что все запросы принадлежат пользователю 1
        assertThat(requests).allMatch(r -> r.getRequestorId().equals(1L));
    }

    @Test
    void testFindAllByRequestIdOrderByCreatedDesc() {
        Long id = request3.getId();
        // Загружаем по ID
        ItemRequest fetchedRequest = itemRequestRepository.findAllByRequestIdOrderByCreatedDesc(id);

        assertThat(fetchedRequest).isNotNull();
        assertThat(fetchedRequest.getId()).isEqualTo(id);
        assertThat(fetchedRequest.getRequestorId()).isEqualTo(userId2);
    }

    @Test
    void testFindAllByOrderByCreatedDesc() {
        List<ItemRequest> allRequests = itemRequestRepository.findAllByOrderByCreatedDesc();

        assertThat(allRequests).isNotEmpty();
        // Проверяем, что запросы отсортированы по времени по убыванию
        for (int i = 0; i < allRequests.size() - 1; i++) {
            assertThat(allRequests.get(i).getCreated()).isAfterOrEqualTo(allRequests.get(i + 1).getCreated());
        }
    }

    @Test
    void testSaveItemRequest() {
        // Проверяем, что у сохраненного объекта появился ID
        assertThat(request1.getId()).isNotNull();
        // Проверяем, что данные сохранены правильно
        assertThat(request1.getRequestorId()).isEqualTo(user1.getId());
        assertThat(request1.getCreated()).isEqualTo(NOW.minusDays(2));
        // Дополнительно можно проверить, что он действительно есть в репозитории
        Optional<ItemRequest> retrieved = itemRequestRepository.findById(request1.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getRequestorId()).isEqualTo(user1.getId());
    }
}