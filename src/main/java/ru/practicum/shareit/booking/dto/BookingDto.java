package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemBookingDto item;
    private BookerDto booker;
    private BookingStatus status;

    public BookingDto(Long id, LocalDateTime start, LocalDateTime end, BookerDto booker, BookingStatus status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.booker = booker;
        this.status = status;
    }
}
