package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.ItemBookingDto;
import ru.practicum.shareit.exception.AvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    @Autowired(required = false)
    private ItemService itemService;
    private final UserService userService;
    private static final String NOT_FOUND_ITEM_MESSAGE = "Вещь не найдена";
    private static final String NOT_FOUND_BOOKING_MESSAGE = "Бронирование не найдено";

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        log.info("Получен запрос на получение конкретного бронирования: {}", bookingId);
        baseValidate(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOKING_MESSAGE));
        ItemDto itemDto = itemService.getItem(booking.getItemId());
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
            throw new ValidationException(NOT_FOUND_ITEM_MESSAGE);
        }
        ItemDto itemDto = itemService.getItem(itemId);
        validate(bookingRequestDto, itemDto, userId);

        // Создаем бронирование в статусе WAITING
        Booking booking = BookingMapper.toBooking(bookingRequestDto);
        hasTimeConflict(booking);
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
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOKING_MESSAGE));

        // Проверяем, что текущий пользователь является владельцем вещи
        ItemDto itemDto = itemService.getItem(booking.getItemId());
        if (!itemDto.getOwnerId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем вещи");
        }

        // Обновляем статус бронирования
        if (booking.getStatus() == BookingStatus.WAITING) {
            if (approved) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
        } else {
            // Логика для случая, когда бронирование уже отменено или завершено
            throw new ValidationException("бронирование уже отменено или завершено");
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

        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "CURRENT":
                bookings = repository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case "PAST":
                bookings = repository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case "FUTURE":
                bookings = repository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            default:
                bookings = repository.findAllByBookerIdOrderByStartDesc(userId);
                break;
        }

        HashSet<Long> itemIds = new HashSet<>();
        HashMap<Long, String> itemsMap  = new HashMap<>();
        for (Booking booking : bookings) {
            itemIds.add(booking.getItemId());
        }
        for (Long itemId : itemIds) {
            ItemDto itemDto = itemService.getItem(itemId);
            itemsMap.put(itemDto.getId(), itemDto.getName());
        }

        return BookingMapper.toBookingDto(bookings, itemsMap);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        log.info("Получение списка бронирований для всех вещей текущего пользователя: {}", userId);
        baseValidate(userId);
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "CURRENT":
                bookings = repository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case "PAST":
                bookings = repository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case "FUTURE":
                bookings = repository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            default:
                bookings = repository.findAllByBookerIdOrderByStartDesc(userId);
                break;
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getItemBookings(Long itemId) {
        return repository.findAllByItemId(itemId);
    }

    @Override
    public List<Booking> getBookerBookings(Long userId) {
        return repository.findAllByBookerIdOrderByStartDesc(userId);
    }

    @Override
    public List<Booking> getBookingsForItems(List<Item> items) {
        // Получаем список идентификаторов элементов
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Делаем один запрос к репозиторию для получения всех бронирований по спискам элементов
        return repository.findAllByItemIdIn(itemIds);
    }

    private boolean hasTimeConflict(Booking newBooking) {
        List<Booking> existingBookings = repository.findAllByItemId(newBooking.getItemId());

        for (Booking existingBooking : existingBookings) {
            if (existingBooking.getStatus() != BookingStatus.REJECTED &&
                    !existingBooking.equals(newBooking) &&
                    (newBooking.getStart().isBefore(existingBooking.getEnd()) && existingBooking.getStart().isBefore(newBooking.getEnd()))) {
                return true; // Пересечение по времени найдено
            }
        }
        return false; // Пересечений по времени нет
    }

    private void baseValidate(Long userId) {
        if (!userService.existsUser(userId)) {
            String errorMessage = String.format("Не найден пользователь с ID %d", userId);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private void validate(BookingRequestDto bookingRequestDto, ItemDto itemDto, Long userId) {
        // снова проверяем на существование вещи
        if (itemDto == null) {
            log.warn(NOT_FOUND_ITEM_MESSAGE);
            throw new NotFoundException(NOT_FOUND_ITEM_MESSAGE);
        }
        baseValidate(userId);
        if (!itemDto.getAvailable()) {
            throw new AvailableException("Вещь недоступна");
        }
        if (bookingRequestDto.getStart() == bookingRequestDto.getEnd()) {
            throw new ValidationException("Дата начала брони не может быть равна дате конца брони");
        }
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата конца брони не может быть раньше даты начала брони");
        }
    }
}