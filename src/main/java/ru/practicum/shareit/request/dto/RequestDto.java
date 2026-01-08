package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RequestDto {
    @NotNull(message = "Описание запроса не может быть пустым")
    private String description;
    private LocalDate created;
}
