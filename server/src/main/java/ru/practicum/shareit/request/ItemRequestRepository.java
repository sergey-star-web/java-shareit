package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("SELECT r FROM ItemRequest r LEFT JOIN FETCH r.items WHERE r.requestorId = :userId ORDER BY r.created DESC")
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM ItemRequest r LEFT JOIN FETCH r.items WHERE r.id = :requestId ORDER BY r.created DESC")
    ItemRequest findAllByRequestIdOrderByCreatedDesc(@Param("requestId") Long requestId);

    List<ItemRequest> findAllByOrderByCreatedDesc();
}