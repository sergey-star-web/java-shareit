package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    @NotNull(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 255, message = "Длина комментария должна быть от 1 до 255 символов")
    private String text;
    private Item item;
    private String authorName;
    private LocalDateTime created;
}