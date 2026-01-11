package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.AvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
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

    @Test
    void testAddBooking_ItemNotAvailable_ShouldThrowAvailableException() {
        // 1. Создаем пользователя
        User user = new User();
        user.setName("Пользователь");
        user.setEmail("user_available_exception@example.com");
        user.setId(userService.addUser(UserMapper.toUserDto(user)).getId());
        Long userId = user.getId();

        // 2. Создаем предмет, сделав его недоступным
        Item item = new Item();
        item.setName("Недоступный предмет");
        item.setDescription("descript");
        item.setAvailable(false);  // делаем недоступным
        item.setOwnerId(userId);
        item.setId(itemService.addItem(userId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        // 3. Создаем BookingRequestDto для этого предмета
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemId);

        // 4. Проверка, что вызов метода вызывает исключение AvailableException
        assertThatThrownBy(() -> {
            bookingService.addBooking(userId, bookingRequestDto);
        }).isInstanceOf(AvailableException.class)
                .hasMessage("Вещь недоступна");
    }

    @Test
    void testGetOwnerBookings_CurrentsPastFutureWaitingRejectedAll() {
        // 1. Создаем пользователя- владельца
        User owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@example.com");
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long ownerId = owner.getId();

        // 2. Создаем предмет, принадлежащий владельцу
        Item item = new Item();
        item.setName("Предмет владельца");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwnerId(ownerId);
        item.setId(itemService.addItem(ownerId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        // 3. Создаем другого пользователя - арендующего
        User booker = new User();
        booker.setName("Арендатор");
        booker.setEmail("booker@example.com");
        booker.setId(userService.addUser(UserMapper.toUserDto(booker)).getId());
        Long bookerId = booker.getId();

        LocalDateTime now = LocalDateTime.now();

        // 4. Создаем бронирования с разными статусами и временами для владельца
        // CURRENT бронирование
        Booking currentBooking = new Booking();
        currentBooking.setItemId(itemId);
        currentBooking.setItem(item);
        currentBooking.setBookerId(bookerId);
        currentBooking.setStart(now.minusDays(1));
        currentBooking.setEnd(now.plusDays(1));
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(currentBooking);

        // PAST бронирование
        Booking pastBooking = new Booking();
        pastBooking.setItemId(itemId);
        pastBooking.setItem(item);
        pastBooking.setBookerId(bookerId);
        pastBooking.setStart(now.minusDays(5));
        pastBooking.setEnd(now.minusDays(3));
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(pastBooking);

        // FUTURE бронирование
        Booking futureBooking = new Booking();
        futureBooking.setItemId(itemId);
        futureBooking.setItem(item);
        futureBooking.setBookerId(bookerId);
        futureBooking.setStart(now.plusDays(2));
        futureBooking.setEnd(now.plusDays(4));
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(futureBooking);

        // WAITING бронирование
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(bookerId);
        waitingBooking.setStart(now.plusDays(1));
        waitingBooking.setEnd(now.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // REJECTED бронирование
        Booking rejectedBooking = new Booking();
        rejectedBooking.setItemId(itemId);
        rejectedBooking.setItem(item);
        rejectedBooking.setBookerId(bookerId);
        rejectedBooking.setStart(now.minusDays(2));
        rejectedBooking.setEnd(now.minusDays(1));
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingService.addBooking(rejectedBooking);

        // 5. Проверка для каждого состояния

        // CURRENT:
        List<BookingDto> currentList = bookingService.getOwnerBookings(bookerId, BookingState.CURRENT);
        assertThat(currentList).isNotEmpty();
        for (BookingDto b : currentList) {
            LocalDateTime start = b.getStart();
            LocalDateTime end = b.getEnd();
            assertThat(start).isBeforeOrEqualTo(now);
            assertThat(end).isAfterOrEqualTo(now);
        }

        // PAST:
        List<BookingDto> pastList = bookingService.getOwnerBookings(bookerId, BookingState.PAST);
        assertThat(pastList).isNotEmpty();
        for (BookingDto b : pastList) {
            assertThat(b.getEnd()).isBeforeOrEqualTo(now);
        }

        // FUTURE:
        List<BookingDto> futureList = bookingService.getOwnerBookings(bookerId, BookingState.FUTURE);
        assertThat(futureList).isNotEmpty();
        for (BookingDto b : futureList) {
            assertThat(b.getStart()).isAfter(now);
        }

        // WAITING:
        List<BookingDto> waitingList = bookingService.getOwnerBookings(bookerId, BookingState.WAITING);
        assertThat(waitingList).isNotEmpty();
        assertThat(waitingList)
                .allSatisfy(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.WAITING));

        // REJECTED:
        List<BookingDto> rejectedList = bookingService.getOwnerBookings(bookerId, BookingState.REJECTED);
        assertThat(rejectedList).isNotEmpty();
        assertThat(rejectedList)
                .allSatisfy(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.REJECTED));

        // ALL:
        List<BookingDto> allList = bookingService.getOwnerBookings(bookerId, BookingState.ALL);
        assertThat(allList).isNotEmpty();
        // тут можно проверить, что все бронирования в списке, или их количество соответствует сделанным
        assertThat(allList.size()).isEqualTo(5);
    }

    @Test
    public void testUpdateBookingApprove() {
        // Создаем пользователя-арендодателя и пользователя-арендатора
        User owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@example.com");
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long ownerId = owner.getId();

        User renter = new User();
        renter.setName("Renter");
        renter.setEmail("Renter@example.com");
        renter.setId(userService.addUser(UserMapper.toUserDto(renter)).getId());
        Long renterId = renter.getId();

        // Создаем предмет и привязываем его владельцу
        // 2. Создаем предмет, принадлежащий владельцу
        Item item = new Item();
        item.setName("Предмет владельца");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwnerId(ownerId);
        item.setId(itemService.addItem(ownerId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        LocalDateTime now = LocalDateTime.now();
        // Создаем бронирование со статусом WAITING
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(renterId);
        waitingBooking.setStart(now.plusDays(1));
        waitingBooking.setEnd(now.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // Обновляем бронирование с одобрением
        bookingService.updateBooking(owner.getId(), waitingBooking.getId(), true);

        // Проверяем, что статус стал APPROVED
        BookingDto updatedBooking = bookingService.getBooking(renterId, waitingBooking.getId());
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    public void testUpdateBookingReject() {
        // Создаем пользователя-арендодателя
        User owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@example.com");
        Long ownerId = userService.addUser(UserMapper.toUserDto(owner)).getId();

        // Создаем пользователя-арендатора
        User renter = new User();
        renter.setName("Renter");
        renter.setEmail("Renter@example.com");
        Long renterId = userService.addUser(UserMapper.toUserDto(renter)).getId();

        // Создаем предмет и привязываем его владельцу
        Item item = new Item();
        item.setName("Предмет владельца");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwnerId(ownerId);
        item.setId(itemService.addItem(ownerId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        LocalDateTime now = LocalDateTime.now();

        // Создаем бронирование со статусом WAITING
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(renterId);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        // Добавляем бронирование
        booking.setId(bookingService.addBooking(booking).getId());

        // Отклоняем бронирование
        bookingService.updateBooking(ownerId, booking.getId(), false);

        // Получаем обновленное бронирование
        BookingDto updatedBooking = bookingService.getBooking(renterId, booking.getId());

        // Проверяем, что статус стал REJECTED
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    public void testUpdateBookingNotFound() {
        Long fakeBookingId = 999L;
        Long userId = 1L;
        assertThrows(NotFoundException.class, () -> {
            bookingService.updateBooking(userId, fakeBookingId, true);
        });
    }

    @Test
    public void testUpdateBookingNotOwner() {
        // Создаем пользователей
        User owner1 = new User();
        owner1.setName("Owner1");
        owner1.setEmail("owner1@example.com");
        Long owner1Id = userService.addUser(UserMapper.toUserDto(owner1)).getId();

        User owner2 = new User();
        owner2.setName("Owner2");
        owner2.setEmail("owner2@example.com");
        Long owner2Id = userService.addUser(UserMapper.toUserDto(owner2)).getId();

        User renter = new User();
        renter.setName("Renter");
        renter.setEmail("renter@example.com");
        Long renterId = userService.addUser(UserMapper.toUserDto(renter)).getId();

        // Создаем предмет у owner1
        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwnerId(owner1Id);
        item.setId(itemService.addItem(owner1Id, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        LocalDateTime now = LocalDateTime.now();

        // Создаем бронирование со статусом WAITING
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(renterId);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        // Добавляем бронирование
        bookingService.addBooking(booking);

        // Попытка обновить бронирование владельцем другого пользователя (owner2)
        assertThrows(ValidationException.class, () -> {
            bookingService.updateBooking(owner2.getId(), booking.getId(), true);
        });
    }

    @Test
    public void testUpdateBookingAlreadyProcessed() {
        // Создаем пользователей
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        Long ownerId = userService.addUser(UserMapper.toUserDto(owner)).getId();

        User renter = new User();
        renter.setName("Renter");
        renter.setEmail("renter@example.com");
        Long renterId = userService.addUser(UserMapper.toUserDto(renter)).getId();

        // Создаем предмет
        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwnerId(ownerId);
        item.setId(itemService.addItem(ownerId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        LocalDateTime now = LocalDateTime.now();

        // Создаем бронирование со статусом APPROVED
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(renterId);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);

        // Добавляем бронирование
        bookingService.addBooking(booking);

        // Попытка повторного обновления (например, изменить статус)
        assertThrows(ValidationException.class, () -> {
            bookingService.updateBooking(ownerId, booking.getId(), false);
        });
    }

}