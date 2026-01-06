package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemId(Long itemId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = :status " +
            "AND (:start < b.end AND b.start < :end)")
    List<Booking> findAllByItemIdAndStatusAndConflict(@Param("itemId") Long itemId,
                                                      @Param("status") BookingStatus status,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    @EntityGraph(attributePaths = {"item"})
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                             LocalDateTime end);

    @EntityGraph(attributePaths = {"item"})
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    @EntityGraph(attributePaths = {"item"})
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime end);

    @EntityGraph(attributePaths = {"item"})
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Поиск всех бронирований по идентификатору букера
    @EntityGraph(attributePaths = {"item"})
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItemIdIn(List<Long> itemIds);
}