package ru.practicum.shareit.base;

import org.springframework.data.jpa.repository.Query;

public interface BaseRepository {
    @Query(value = "SELECT EXISTS (SELECT 1 FROM users WHERE id = ?1)", nativeQuery = true)
    Boolean isUserExist(Long userId);
}
