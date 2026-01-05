package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ItemListDto {
    private Long id;
    private String name;
    private LocalDateTime lastBookingDate;
    private LocalDateTime nextBookingDate;
}
