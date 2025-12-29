package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.ItemBookingDto;
import ru.practicum.shareit.exception.AvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final ItemServiceImpl itemServiceImpl;
    private final String notFoundItemMessage = "Вещь не найдена";

    @Override
    public BookingDto getBooking(Long userId) {
        return repository.findById(userId)
                .map(BookingMapper::toBookingDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Получен запрос на добавление бронирования: {}", bookingRequestDto);
        Long itemId = bookingRequestDto.getItemId();
        if (itemId == null) {
            throw new ValidationException(notFoundItemMessage);
        }
        ItemDto itemDto = itemServiceImpl.getItem(userId, itemId);
        validate(bookingRequestDto, itemDto, userId);

        // Создаем бронирование в статусе WAITING
        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItemId(itemDto.getId());
        booking.setBookerId(userId);
        booking.setStatus(BookingStatus.WAITING);

        BookingDto rezBookingDto = BookingMapper.toBookingDto(repository.save(booking));
        ItemBookingDto itemBookingDto = new ItemBookingDto(itemDto.getId(), itemDto.getName());
        rezBookingDto.setItem(itemBookingDto);
        return rezBookingDto;
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long userId, Long bookingId, Boolean approved) {
        return null;
    }

    private void throwIfNoItem() {
        log.warn(notFoundItemMessage);
        throw new NotFoundException(notFoundItemMessage);
    }

    private void validate(BookingRequestDto bookingRequestDto, ItemDto itemDto, Long userId) {
        // снова проверяем на существование вещи
        if (itemDto == null) {
            throwIfNoItem();
        }
        if (!repository.isUserExist(userId)) {
            String errorMessage = String.format("Не найден пользователь с ID %d", userId);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        if (!itemDto.getAvailable()) {
            throw new AvailableException("Вещь недоступна");
        }
        if (bookingRequestDto.getStart() == null) {
            throw new ValidationException("Дата начала брони не может быть пустым");
        }
        if (bookingRequestDto.getEnd() == null) {
            throw new ValidationException("Дата конца брони не может быть пустым");
        }
        if (bookingRequestDto.getStart() == bookingRequestDto.getEnd()) {
            throw new ValidationException("Дата начала брони не может быть равна дате конца брони");
        }
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата конца брони не может быть раньше даты начала брони");
        }
    }
}
