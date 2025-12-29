package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

public interface BookingService {
    BookingDto getBooking(Long userId);

    BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingDto updateBooking(Long userId, Long bookingId, Boolean approved);
}
