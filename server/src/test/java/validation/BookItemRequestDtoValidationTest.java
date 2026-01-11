package validation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ru.practicum.shareit.booking.dto.BookingRequestDto;

public class BookItemRequestDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateValidDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenStartIsNull() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(null);
        dto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("start")
                        && v.getMessage().contains("не может быть пустым"));
    }

    @Test
    void shouldFailWhenEndIsNull() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(null);

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")
                        && v.getMessage().contains("не может быть пустым"));
    }

    @Test
    void shouldFailWhenStartIsInPast() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().minusSeconds(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("start")
                        && v.getMessage().contains("должна быть в будущем"));
    }

    @Test
    void shouldFailWhenEndIsInPast() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().minusSeconds(1));

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("end")
                        && v.getMessage().contains("должна быть в будущем"));
    }
}
