package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemListDto {
    private Long id;
    private String name;
    private LocalDateTime lastBookingDate;
    private LocalDateTime nextBookingDate;
}
