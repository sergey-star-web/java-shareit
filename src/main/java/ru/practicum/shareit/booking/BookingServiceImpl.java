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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final ItemServiceImpl itemServiceImpl;
    private final String notFoundItemMessage = "Вещь не найдена";
    private final String notFoundBookingMessage = "Бронирование не найдено";

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        log.info("Получен запрос на получение конкретного бронирования: {}", bookingId);
        baseValidate(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(notFoundBookingMessage));
        ItemDto itemDto = itemServiceImpl.getItem(booking.getItemId());
        // Проверяем, что текущий пользователь является автором бронирования или владельцем вещи
        if (!booking.getBookerId().equals(userId) && !itemDto.getOwnerId().equals(userId)) {
            throw new ValidationException("У вас нет прав на текущее бронирование");
        }

        ItemBookingDto itemBookingDto = new ItemBookingDto(itemDto.getId(), itemDto.getName());
        BookingDto rezBookingDto = BookingMapper.toBookingDto(booking);
        rezBookingDto.setItem(itemBookingDto);
        return rezBookingDto;
    }

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto) {
        log.info("Получен запрос на добавление бронирования: {}", bookingRequestDto);
        Long itemId = bookingRequestDto.getItemId();
        if (itemId == null) {
            throw new ValidationException(notFoundItemMessage);
        }
        ItemDto itemDto = itemServiceImpl.getItem(itemId);
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
        log.info("Бронь успешно добавлена: " + rezBookingDto + " поль-ель: " + userId);
        return rezBookingDto;
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Получен запрос на обновление брони: " + bookingId + " status: " + approved + " поль-ель: " + userId);
        // Находим бронирование по bookingId
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(notFoundBookingMessage));

        // Проверяем, что текущий пользователь является владельцем вещи
        ItemDto itemDto = itemServiceImpl.getItem(booking.getItemId());
        if (!itemDto.getOwnerId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем вещи");
        }

        // Обновляем статус бронирования
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        // Сохраняем изменения
        BookingDto updatedBookingDto = BookingMapper.toBookingDto(repository.save(booking));
        ItemBookingDto itemBookingDto = new ItemBookingDto(itemDto.getId(), itemDto.getName());
        updatedBookingDto.setItem(itemBookingDto);
        log.info("Бронь успешно обновлена: " + updatedBookingDto + " поль-ель: " + userId);
        // Возвращаем обновлённый BookingDto
        return updatedBookingDto;
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        log.info("Получен запрос на получение бронирований текущего поль-ля: {}", userId);
        baseValidate(userId);
        List<Booking> bookings;

        switch (state) {
            case "CURRENT":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.CURRENT);
                break;
            case "PAST":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.PAST);
                break;
            case "FUTURE":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.FUTURE);
                break;
            case "WAITING":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = repository.findAllByBookerId(userId);
                break;
        }

        // Сортируем бронирования по дате от более новых к более старым
        bookings.sort((b1, b2) -> b2.getStart().compareTo(b1.getStart()));
        HashSet<Long> itemIds = new HashSet<>();
        HashMap<Long, String> itemsMap  = new HashMap<>();
        for (Booking booking : bookings) {
            itemIds.add(booking.getItemId());
        }
        for (Long itemId : itemIds) {
            ItemDto itemDto = itemServiceImpl.getItem(itemId);
            itemsMap.put(itemDto.getId(), itemDto.getName());
        }

        return BookingMapper.toBookingDto(bookings, itemsMap);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        log.info("Получение списка бронирований для всех вещей текущего пользователя: {}", userId);
        baseValidate(userId);
        List<Booking> bookings;

        switch (state) {
            case "CURRENT":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.CURRENT);
                break;
            case "PAST":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.PAST);
                break;
            case "FUTURE":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.FUTURE);
                break;
            case "WAITING":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = repository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = repository.findAllByBookerId(userId);
                break;
        }

        // Сортируем бронирования по дате от более новых к более старым
        bookings.sort((b1, b2) -> b2.getStart().compareTo(b1.getStart()));

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void throwIfNoItem() {
        log.warn(notFoundItemMessage);
        throw new NotFoundException(notFoundItemMessage);
    }

    private void baseValidate(Long userId) {
        if (!repository.isUserExist(userId)) {
            String errorMessage = String.format("Не найден пользователь с ID %d", userId);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private void validate(BookingRequestDto bookingRequestDto, ItemDto itemDto, Long userId) {
        // снова проверяем на существование вещи
        if (itemDto == null) {
            throwIfNoItem();
        }
        baseValidate(userId);
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
