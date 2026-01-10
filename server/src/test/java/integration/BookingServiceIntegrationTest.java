package integration;

import config.AppConfig;
import config.PersistenceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
@SpringJUnitConfig({UserServiceImpl.class, BookingServiceImpl.class, ItemServiceImpl.class, PersistenceConfig.class})
public class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @Test
    void testGetUserBookings_CurrentsPastFutureWaitingRejected() {
        // 1. Создаем пользователя
        User user = new User();
        user.setName("Пользователь");
        user.setEmail("user@example.com");
        user.setId(userService.addUser(UserMapper.toUserDto(user)).getId());

        Long userId = user.getId();

        // 2. Создаем предмет
        Item item = new Item();
        item.setName("Предмет 1");
        item.setDescription("descript");
        item.setAvailable(true);
        item.setOwnerId(userId);
        item.setId(itemService.addItem(userId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        LocalDateTime now = LocalDateTime.now();

        // 3. Создаем бронирования для каждого режима

        // Текущее: start -1 день, end +1 день (должно попасть в CURRENT)
        Booking currentBooking = new Booking();
        currentBooking.setItemId(itemId);
        currentBooking.setItem(item);
        currentBooking.setBookerId(userId);
        currentBooking.setStart(now.minusDays(1));
        currentBooking.setEnd(now.plusDays(1));
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(currentBooking);

        // Прошедшее: start -5 дней, end -3 дня (должно попасть в PAST)
        Booking pastBooking = new Booking();
        pastBooking.setItemId(itemId);
        pastBooking.setItem(item);
        pastBooking.setBookerId(userId);
        pastBooking.setStart(now.minusDays(5));
        pastBooking.setEnd(now.minusDays(3));
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(pastBooking);

        // Будущее: start +2 дня, end +4 дня (должно попасть в FUTURE)
        Booking futureBooking = new Booking();
        futureBooking.setItemId(itemId);
        futureBooking.setItem(item);
        futureBooking.setBookerId(userId);
        futureBooking.setStart(now.plusDays(2));
        futureBooking.setEnd(now.plusDays(4));
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(futureBooking);

        // WAITING: start +1 день, end +2 дня (должно попасть в WAITING)
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(userId);
        waitingBooking.setStart(now.plusDays(1));
        waitingBooking.setEnd(now.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // Rejected: start -2 дня, end -1 день (должно попасть в REJECTED)
        Booking rejectedBooking = new Booking();
        rejectedBooking.setItemId(itemId);
        rejectedBooking.setItem(item);
        rejectedBooking.setBookerId(userId);
        rejectedBooking.setStart(now.minusDays(2));
        rejectedBooking.setEnd(now.minusDays(1));
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingService.addBooking(rejectedBooking);

        // 4. Проверка для каждого состояния
        // CURRENT:
        List<BookingDto> currentBookings = bookingService.getUserBookings(userId, BookingState.CURRENT);
        assertThat(currentBookings).isNotEmpty();
        // Проверяем, что все даты start и end попадают в текущий интервал
        for (BookingDto b : currentBookings) {
            LocalDateTime start = b.getStart();
            LocalDateTime end = b.getEnd();
            assertThat(start).isBeforeOrEqualTo(now);
            assertThat(end).isAfterOrEqualTo(now);
        }

        // PAST:
        List<BookingDto> pastBookings = bookingService.getUserBookings(userId, BookingState.PAST);
        assertThat(pastBookings).isNotEmpty();
        for (BookingDto b : pastBookings) {
            assertThat(b.getEnd()).isBeforeOrEqualTo(now);
        }

        // FUTURE:
        List<BookingDto> futureBookings = bookingService.getUserBookings(userId, BookingState.FUTURE);
        assertThat(futureBookings).isNotEmpty();
        for (BookingDto b : futureBookings) {
            assertThat(b.getStart()).isAfter(now);
        }

        // WAITING:
        List<BookingDto> waitingBookings = bookingService.getUserBookings(userId, BookingState.WAITING);
        assertThat(waitingBookings).isNotEmpty();
        assertThat(waitingBookings)
                .allSatisfy(b -> assertThat(b.getStatus().toString()).isEqualTo(BookingStatus.WAITING.name()));

        // REJECTED:
        List<BookingDto> rejectedBookings = bookingService.getUserBookings(userId, BookingState.REJECTED);
        assertThat(rejectedBookings).isNotEmpty();
        assertThat(rejectedBookings)
                .allSatisfy(b -> assertThat(b.getStatus().toString()).isEqualTo(BookingStatus.REJECTED.name()));
    }
}