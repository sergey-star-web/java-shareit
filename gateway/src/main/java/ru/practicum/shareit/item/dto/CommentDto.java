package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    @NotNull(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 255, message = "Длина комментария должна быть от 1 до 255 символов")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
