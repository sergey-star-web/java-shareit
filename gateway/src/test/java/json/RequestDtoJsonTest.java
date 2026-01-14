package json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.request.dto.RequestDto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureJsonTesters
public class RequestDtoJsonTest {
    @Autowired
    private JacksonTester<RequestDto> json;

    @Test
    void serializeRequestDto() throws Exception {
        RequestDto request = new RequestDto();
        request.setDescription("Описание запроса");
        request.setCreated(LocalDate.of(2024, 4, 27));

        var content = json.write(request);

        // Проверка сериализации
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Описание запроса");
        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo("2024-04-27");
    }
}