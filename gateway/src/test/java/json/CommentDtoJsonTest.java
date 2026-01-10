package json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.dto.CommentDto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureJsonTesters
public class CommentDtoJsonTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serializeCommentDto() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Это комментарий");
        dto.setAuthorName("Иван Иванов");
        dto.setCreated(LocalDateTime.of(2024, 4, 27, 15, 30));

        var content = json.write(dto);

        // Проверка сериализации
        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.text").isEqualTo("Это комментарий");
        assertThat(content).extractingJsonPathStringValue("$.authorName").isEqualTo("Иван Иванов");
        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo("2024-04-27T15:30:00");
    }
}
