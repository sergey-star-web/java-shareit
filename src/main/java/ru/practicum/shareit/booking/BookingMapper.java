package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.ItemBookingDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookerDto bookerDto = new BookerDto(booking.getBookerId());
        ItemBookingDto itemBookingDto = new ItemBookingDto(booking.getItemId());
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemBookingDto,
                bookerDto,
                booking.getStatus()
        );
    }

    public static List<BookingDto> toBookingDto(List<Booking> bookings, HashMap<Long, String> items) {
        List<BookingDto> bookingDtos = new ArrayList<>();

        for (Booking booking : bookings) {
            BookingDto bookingDto = new BookingDto();
            bookingDto.setId(booking.getId());
            bookingDto.setStart(booking.getStart());
            bookingDto.setEnd(booking.getEnd());
            bookingDto.setBooker(new BookerDto(booking.getBookerId()));
            bookingDto.setItem(new ItemBookingDto(booking.getItemId(), items.get(booking.getItemId())));
            bookingDto.setStatus(booking.getStatus());

            bookingDtos.add(bookingDto);
        }

        return bookingDtos;
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

    public static BookingRequestDto toBookingRequestDto(Booking booking) {
        return new BookingRequestDto(booking.getStart(), booking.getEnd(), booking.getItemId());
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto) {
        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItemId(bookingRequestDto.getItemId());
        return booking;
    }
}