package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingDate {
    private LocalDateTime lastBookingDate;
    private LocalDateTime nextBookingDate;
}
