package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.base.BaseRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, BaseRepository {
    List<Booking> findAllByItemId(Long itemId);

    // Поиск всех бронирований по идентификатору букера и статусу
    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status);

    // Поиск всех бронирований по идентификатору букера
    List<Booking> findAllByBookerId(Long bookerId);
}
