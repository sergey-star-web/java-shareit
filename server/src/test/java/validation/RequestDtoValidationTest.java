package validation;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.RequestDto;

import java.time.LocalDate;
import java.util.Set;

public class RequestDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldValidateCorrectRequestDto() {
        RequestDto request = new RequestDto();
        request.setDescription("Описание запроса");
        request.setCreated(LocalDate.of(2024, 4, 27));

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenDescriptionIsNull() {
        RequestDto request = new RequestDto();
        request.setDescription(null); // Нарушение @NotNull
        request.setCreated(LocalDate.of(2024, 4, 27));

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")
                        && v.getMessage().contains("не может быть пустым"));
    }

    @Test
    void shouldAllowNullCreated() {
        RequestDto request = new RequestDto();
        request.setDescription("Описание запроса");
        request.setCreated(null); // Поле с @NotNull только для description, поэтому это допустимо

        Set<ConstraintViolation<RequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}