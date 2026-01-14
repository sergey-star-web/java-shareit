package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Long userId1;
    private Long userId2;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
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

        item1 = new Item();
        item1.setName("Книга");
        item1.setDescription("Интересная книга о программировании");
        item1.setOwnerId(userId1);
        item1.setAvailable(true);
        itemRepository.save(item1);

        item2 = new Item();
        item2.setName("Мороженое");
        item2.setDescription("Мороженое ванильного вкуса");
        item2.setOwnerId(userId2);
        item2.setAvailable(true);
        itemRepository.save(item2);

        item3 = new Item();
        item3.setName("Велосипед");
        item3.setDescription("Горный велосипед");
        item3.setOwnerId(userId1);
        item3.setAvailable(true);
        itemRepository.save(item3);
    }

    @Test
    void testSaveAndFindById() {
        // Создаем новый предмет
        Item newItem = new Item();
        newItem.setName("Игрушка");
        newItem.setDescription("Мягкая игрушка");
        newItem.setAvailable(true);
        newItem.setOwnerId(userId2);

        // Сохраняем
        Item savedItem = itemRepository.save(newItem);
        // Проверка что у сохраненного есть id
        assertThat(savedItem.getId()).isNotNull();

        // Извлекаем по id
        Optional<Item> retrieved = itemRepository.findById(savedItem.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Игрушка");
        assertThat(retrieved.get().getOwnerId()).isEqualTo(userId2);
        assertThat(retrieved.get().getAvailable()).isTrue();
    }

    @Test
    void testFindByOwnerId() {
        // Находим все товары владельца с id 1
        List<Item> ownerItems = itemRepository.findByOwnerId(userId1);

        assertThat(ownerItems).isNotEmpty();
        assertThat(ownerItems.size()).isEqualTo(2);
        // Проверка, что все товары принадлежат владельцу 1
        assertThat(ownerItems).allMatch(item -> item.getOwnerId().equals(userId1));
    }

    @Test
    void testDelete() {
        // Удаляем предмет
        Long idToDelete = item2.getId();
        itemRepository.deleteById(idToDelete);
        // Проверка, что он удален
        Optional<Item> deletedItem = itemRepository.findById(idToDelete);
        assertThat(deletedItem).isNotPresent();
    }

    @Test
    void testFindAll() {
        List<Item> items = itemRepository.findAll();
        assertThat(items).hasSize(3);
    }
}
