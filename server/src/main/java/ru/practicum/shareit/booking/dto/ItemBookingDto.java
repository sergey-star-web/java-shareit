package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemBookingDto {
    private Long id;
    private String name;

    public ItemBookingDto(Long id) {
        this.id = id;
    }
}
