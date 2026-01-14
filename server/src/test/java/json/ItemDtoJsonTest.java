package json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureJsonTesters
public class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serializeItemDto() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Item Description");
        item.setAvailable(true);
        item.setOwnerId(42L);
        item.setRequestId(100L);

        // Выполняем сериализацию
        var content = json.write(item);

        // Проверяем отдельные поля
        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Item Description");
        assertThat(content).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(content).extractingJsonPathNumberValue("$.ownerId").isEqualTo(42);
        assertThat(content).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);
    }
}
