package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    @Mock
    private BookingService bookingService;
    @InjectMocks
    private BookingController controller;
    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Вспомогательные методы для создания DTO
    private BookingDto createBookingDto(Long id, String status) {
        BookingDto dto = new BookingDto();
        dto.setId(id);
        dto.setStatus(BookingStatus.valueOf(status));
        return dto;
    }

    private BookingRequestDto createBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(111L);
        return dto;
    }

    @Test
    void testCreateBooking() throws Exception {
        Long userId = 1L;
        BookingRequestDto requestDto = createBookingRequestDto();
        BookingDto responseDto = createBookingDto(100L, "WAITING");
        when(bookingService.addBooking(eq(userId), any(BookingRequestDto.class))).thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).addBooking(eq(userId), any(BookingRequestDto.class));
    }

    @Test
    void testUpdateBookingStatus() throws Exception {
        Long userId = 2L;
        Long bookingId = 55L;
        Boolean approved = true;
        BookingDto responseDto = createBookingDto(bookingId, "APPROVED");
        when(bookingService.updateBooking(userId, bookingId, approved)).thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).updateBooking(userId, bookingId, approved);
    }

    @Test
    void testGetBooking() throws Exception {
        Long userId = 3L;
        Long bookingId = 77L;
        BookingDto responseDto = createBookingDto(bookingId, "WAITING");
        when(bookingService.getBooking(userId, bookingId)).thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).getBooking(userId, bookingId);
    }

    @Test
    void testGetUserBookings() throws Exception {
        Long userId = 4L;
        String state = "ALL";
        List<BookingDto> bookings = Arrays.asList(
                createBookingDto(1L, "WAITING"),
                createBookingDto(2L, "APPROVED")
        );
        when(bookingService.getUserBookings(eq(userId), eq(BookingState.valueOf(state)))).thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));

        verify(bookingService).getUserBookings(eq(userId), eq(BookingState.valueOf(state)));
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        Long userId = 5L;
        String state = "ALL";
        List<BookingDto> bookings = Arrays.asList(
                createBookingDto(3L, "REJECTED"),
                createBookingDto(4L, "WAITING")
        );
        when(bookingService.getOwnerBookings(eq(userId), eq(BookingState.valueOf(state)))).thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].status").value("WAITING"));

        verify(bookingService).getOwnerBookings(eq(userId), eq(BookingState.valueOf(state)));
    }
}