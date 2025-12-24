package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;

    @Override
    public BookingDto getBooking(Long userId) {
        return repository.findById(userId)
                .map(BookingMapper::toBookingDto)
                .orElse(null);
    }

    @Override
    public BookingDto addBooking(UserDto userDto) {
        return null;
    }

    @Override
    public BookingDto updateBooking(Long userId, UserDto userDto) {
        return null;
    }
}
