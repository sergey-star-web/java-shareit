package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Booking {
    private Integer id;
    private LocalDate start;
    private LocalDate end;
    private Integer item;
    private String description;
    private BookingStatus status;
}
