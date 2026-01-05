package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {
    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long userId, String state);

    BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingDto updateBooking(Long userId, Long bookingId, Boolean approved);
}
