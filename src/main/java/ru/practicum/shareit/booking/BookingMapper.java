package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookerDto bookerDto = new BookerDto(booking.getBookerId());
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                bookerDto,
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId(bookingDto.getItem().getId());
        booking.setBookerId(bookingDto.getBooker().getId());
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }
}