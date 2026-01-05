package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.addBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
    @PathVariable Long bookingId, @RequestParam(value = "approved") Boolean approved) {
        return bookingService.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") String state,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getOwnerBookings(userId, state);
    }
}