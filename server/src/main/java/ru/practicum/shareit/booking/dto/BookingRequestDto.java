package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {
    @FutureOrPresent
    @NotNull(message = "Дата начала брони не может быть пустым")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;
    @NotNull(message = "Дата конца брони не может быть пустым")
    @Future(message = "Дата конца должна быть в будущем")
    private LocalDateTime end;
    private Long itemId;
}
