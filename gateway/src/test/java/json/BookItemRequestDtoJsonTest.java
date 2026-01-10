package json;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.json.JacksonTester;

import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureJsonTesters
public class BookItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Test
    void testBookItemRequestDto() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
        ObjectMapper objectMapper = new ObjectMapper();

        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(123L);
        dto.setStart(startDate);
        dto.setEnd(endDate);

        var content = json.write(dto);
        String jsonString = content.getJson();

        // Разбор JSON
        JsonNode node = objectMapper.readTree(jsonString);
        String startStr = node.get("start").asText();
        String endStr = node.get("end").asText();

        LocalDateTime startParsed = LocalDateTime.parse(startStr, formatter);
        LocalDateTime endParsed = LocalDateTime.parse(endStr, formatter);

        // Проверка
        assertThat(startParsed).isEqualTo(startDate);
        assertThat(endParsed).isEqualTo(endDate);
    }
}