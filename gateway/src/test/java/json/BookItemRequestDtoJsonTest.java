package json;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(123L);
        dto.setStart(startDate);
        dto.setEnd(endDate);

        // Здесь, чтобы проверить сериализацию, используем наш ObjectMapper
        var content = json.write(dto);

        // Проверки
        assertThat(content).extractingJsonPathNumberValue("$.itemId").isEqualTo(123);
        assertThat(content).extractingJsonPathStringValue("$.start")
                .isEqualTo(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")));
        assertThat(content).extractingJsonPathStringValue("$.end")
                .isEqualTo(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")));
    }
}