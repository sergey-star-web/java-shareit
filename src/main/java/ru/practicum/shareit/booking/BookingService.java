package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.user.dto.UserDto;

public interface BookingService {
    BookingDto getBooking(Long userId);

    BookingDto addBooking(UserDto userDto);

    BookingDto updateBooking(Long userId, UserDto userDto);
}
