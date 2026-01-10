package validation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;

public class CommentDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateCorrectCommentDto() {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Это комментарий");
        dto.setAuthorName("Иван Иванов");
        dto.setCreated(LocalDateTime.now());

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenTextIsNull() {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText(null); // Нарушение @NotNull
        dto.setAuthorName("Иван Иванов");
        dto.setCreated(LocalDateTime.now());

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("не может быть пустым"));
    }

    @Test
    void shouldFailWhenTextTooShort() {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText(""); // Меньше 1 символа
        dto.setAuthorName("Иван Иванов");
        dto.setCreated(LocalDateTime.now());

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("Длина комментария должна быть от 1 до 255 символов"));
    }

    @Test
    void shouldFailWhenTextTooLong() {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("a".repeat(256)); // Больше 255 символов
        dto.setAuthorName("Иван Иванов");
        dto.setCreated(LocalDateTime.now());

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("text")
                        && v.getMessage().contains("Длина комментария должна быть от 1 до 255 символов"));
    }
}
