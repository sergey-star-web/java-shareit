package unit;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class BookingServiceTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private User user;
    private User booker;
    private Long userId;
    private Long bookerId;
    private Item item;
    private Long itemId;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Создание владельца
        user = new User();
        user.setName("Владелец");
        user.setEmail("owner@example.com");
        user.setId(userService.addUser(UserMapper.toUserDto(user)).getId());
        userId = user.getId();

        // Создание предмета, принадлежащего владельцу
        item = new Item();
        item.setName("Предмет владельца");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwnerId(userId);
        item.setId(itemService.addItem(userId, ItemMapper.toItemDto(item)).getId());
        itemId = item.getId();

        booker = new User();
        booker.setName("Арендатор");
        booker.setEmail("booker@example.com");
        booker.setId(userService.addUser(UserMapper.toUserDto(booker)).getId());
        bookerId = booker.getId();
    }

    @Test
    void testGetUserBookings_CurrentsPastFutureWaitingRejected() {
        // Текущее: start -1 день, end +1 день (должно попасть в CURRENT)
        Booking currentBooking = new Booking();
        currentBooking.setItemId(itemId);
        currentBooking.setItem(item);
        currentBooking.setBookerId(userId);
        currentBooking.setStart(NOW.minusDays(1));
        currentBooking.setEnd(NOW.plusDays(1));
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(currentBooking);

        // Прошедшее: start -5 дней, end -3 дня (должно попасть в PAST)
        Booking pastBooking = new Booking();
        pastBooking.setItemId(itemId);
        pastBooking.setItem(item);
        pastBooking.setBookerId(userId);
        pastBooking.setStart(NOW.minusDays(5));
        pastBooking.setEnd(NOW.minusDays(3));
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(pastBooking);

        // Будущее: start +2 дня, end +4 дня (должно попасть в FUTURE)
        Booking futureBooking = new Booking();
        futureBooking.setItemId(itemId);
        futureBooking.setItem(item);
        futureBooking.setBookerId(userId);
        futureBooking.setStart(NOW.plusDays(2));
        futureBooking.setEnd(NOW.plusDays(4));
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(futureBooking);

        // WAITING: start +1 день, end +2 дня (должно попасть в WAITING)
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(userId);
        waitingBooking.setStart(NOW.plusDays(1));
        waitingBooking.setEnd(NOW.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // Rejected: start -2 дня, end -1 день (должно попасть в REJECTED)
        Booking rejectedBooking = new Booking();
        rejectedBooking.setItemId(itemId);
        rejectedBooking.setItem(item);
        rejectedBooking.setBookerId(userId);
        rejectedBooking.setStart(NOW.minusDays(2));
        rejectedBooking.setEnd(NOW.minusDays(1));
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingService.addBooking(rejectedBooking);

        // Проверка для каждого состояния
        // CURRENT:
        List<BookingDto> currentBookings = bookingService.getUserBookings(userId, BookingState.CURRENT);
        assertThat(currentBookings).isNotEmpty();
        // Проверяем, что все даты start и end попадают в текущий интервал
        for (BookingDto b : currentBookings) {
            LocalDateTime start = b.getStart();
            LocalDateTime end = b.getEnd();
            assertThat(start).isBeforeOrEqualTo(NOW);
            assertThat(end).isAfterOrEqualTo(NOW);
        }

        // PAST:
        List<BookingDto> pastBookings = bookingService.getUserBookings(userId, BookingState.PAST);
        assertThat(pastBookings).isNotEmpty();
        for (BookingDto b : pastBookings) {
            assertThat(b.getEnd()).isBeforeOrEqualTo(NOW);
        }

        // FUTURE:
        List<BookingDto> futureBookings = bookingService.getUserBookings(userId, BookingState.FUTURE);
        assertThat(futureBookings).isNotEmpty();
        for (BookingDto b : futureBookings) {
            assertThat(b.getStart()).isAfter(NOW);
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
        Item item = new Item();
        item.setName("Недоступный предмет");
        item.setDescription("descript");
        item.setAvailable(false);  // делаем недоступным
        item.setOwnerId(userId);
        item.setId(itemService.addItem(userId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        // Создаем BookingRequestDto для этого предмета
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemId);

        // Проверка, что вызов метода вызывает исключение AvailableException
        assertThatThrownBy(() -> {
            bookingService.addBooking(userId, bookingRequestDto);
        }).isInstanceOf(AvailableException.class)
                .hasMessage("Вещь недоступна");
    }

    @Test
    void testGetOwnerBookings_CurrentsPastFutureWaitingRejectedAll() {
        // Создаем бронирования с разными статусами и временами для владельца
        // CURRENT бронирование
        Booking currentBooking = new Booking();
        currentBooking.setItemId(itemId);
        currentBooking.setItem(item);
        currentBooking.setBookerId(bookerId);
        currentBooking.setStart(NOW.minusDays(1));
        currentBooking.setEnd(NOW.plusDays(1));
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(currentBooking);

        // PAST бронирование
        Booking pastBooking = new Booking();
        pastBooking.setItemId(itemId);
        pastBooking.setItem(item);
        pastBooking.setBookerId(bookerId);
        pastBooking.setStart(NOW.minusDays(5));
        pastBooking.setEnd(NOW.minusDays(3));
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(pastBooking);

        // FUTURE бронирование
        Booking futureBooking = new Booking();
        futureBooking.setItemId(itemId);
        futureBooking.setItem(item);
        futureBooking.setBookerId(bookerId);
        futureBooking.setStart(NOW.plusDays(2));
        futureBooking.setEnd(NOW.plusDays(4));
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(futureBooking);

        // WAITING бронирование
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(bookerId);
        waitingBooking.setStart(NOW.plusDays(1));
        waitingBooking.setEnd(NOW.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // REJECTED бронирование
        Booking rejectedBooking = new Booking();
        rejectedBooking.setItemId(itemId);
        rejectedBooking.setItem(item);
        rejectedBooking.setBookerId(bookerId);
        rejectedBooking.setStart(NOW.minusDays(2));
        rejectedBooking.setEnd(NOW.minusDays(1));
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingService.addBooking(rejectedBooking);

        //Проверка для каждого состояния
        // CURRENT:
        List<BookingDto> currentList = bookingService.getOwnerBookings(bookerId, BookingState.CURRENT);
        assertThat(currentList).isNotEmpty();
        for (BookingDto b : currentList) {
            LocalDateTime start = b.getStart();
            LocalDateTime end = b.getEnd();
            assertThat(start).isBeforeOrEqualTo(NOW);
            assertThat(end).isAfterOrEqualTo(NOW);
        }

        // PAST:
        List<BookingDto> pastList = bookingService.getOwnerBookings(bookerId, BookingState.PAST);
        assertThat(pastList).isNotEmpty();
        for (BookingDto b : pastList) {
            assertThat(b.getEnd()).isBeforeOrEqualTo(NOW);
        }

        // FUTURE:
        List<BookingDto> futureList = bookingService.getOwnerBookings(bookerId, BookingState.FUTURE);
        assertThat(futureList).isNotEmpty();
        for (BookingDto b : futureList) {
            assertThat(b.getStart()).isAfter(NOW);
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
        // Создаем бронирование со статусом WAITING
        Booking waitingBooking = new Booking();
        waitingBooking.setItemId(itemId);
        waitingBooking.setItem(item);
        waitingBooking.setBookerId(bookerId);
        waitingBooking.setStart(NOW.plusDays(1));
        waitingBooking.setEnd(NOW.plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(waitingBooking);

        // Обновляем бронирование с одобрением
        bookingService.updateBooking(user.getId(), waitingBooking.getId(), true);

        // Проверяем, что статус стал APPROVED
        BookingDto updatedBooking = bookingService.getBooking(bookerId, waitingBooking.getId());
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    public void testUpdateBookingReject() {
        // Создаем бронирование со статусом WAITING
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(bookerId);
        booking.setStart(NOW.plusDays(1));
        booking.setEnd(NOW.plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        // Добавляем бронирование
        booking.setId(bookingService.addBooking(booking).getId());

        // Отклоняем бронирование
        bookingService.updateBooking(userId, booking.getId(), false);

        // Получаем обновленное бронирование
        BookingDto updatedBooking = bookingService.getBooking(bookerId, booking.getId());

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
        User owner2 = new User();
        owner2.setName("Owner2");
        owner2.setEmail("owner2@example.com");
        Long owner2Id = userService.addUser(UserMapper.toUserDto(owner2)).getId();

        // Создаем предмет у owner1
        Item item = new Item();
        item.setName("Item1");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwnerId(userId);
        item.setId(itemService.addItem(userId, ItemMapper.toItemDto(item)).getId());
        Long itemId = item.getId();

        // Создаем бронирование со статусом WAITING
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(bookerId);
        booking.setStart(NOW.plusDays(1));
        booking.setEnd(NOW.plusDays(2));
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
        // Создаем бронирование со статусом APPROVED
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(bookerId);
        booking.setStart(NOW.plusDays(1));
        booking.setEnd(NOW.plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);

        // Добавляем бронирование
        bookingService.addBooking(booking);

        // Попытка повторного обновления (например, изменить статус)
        assertThrows(ValidationException.class, () -> {
            bookingService.updateBooking(userId, booking.getId(), false);
        });
    }

    @Test
    public void testGetBookingsForItems_WithTestData() {
        // Создаем вещь (item)
        Item item1 = new Item();
        item1.setName("Item1");
        item1.setDescription("Description1");
        item1.setAvailable(true);
        item1.setOwnerId(userId);
        item1.setId(itemService.addItem(userId, ItemMapper.toItemDto(item1)).getId());

        Item item2 = new Item();
        item2.setName("Item2");
        item2.setDescription("Description2");
        item2.setAvailable(true);
        item2.setOwnerId(userId);
        item2.setId(itemService.addItem(userId, ItemMapper.toItemDto(item2)).getId());

        // Создаем бронирования для предметов
        Booking booking1 = new Booking();
        booking1.setItemId(item1.getId());
        booking1.setItem(item1);
        booking1.setBookerId(bookerId);
        booking1.setStart(NOW.plusDays(1));
        booking1.setEnd(NOW.plusDays(2));
        booking1.setStatus(BookingStatus.APPROVED);

        Booking booking2 = new Booking();
        booking2.setItemId(item2.getId());
        booking2.setItem(item2);
        booking2.setBookerId(bookerId);
        booking2.setStart(NOW.plusDays(3));
        booking2.setEnd(NOW.plusDays(4));
        booking2.setStatus(BookingStatus.APPROVED);

        // Добавляем бронирования (предположим, у вас есть метод сохранения, например, bookingService.addBooking)
        bookingService.addBooking(booking1);
        bookingService.addBooking(booking2);

        // Создаем список Items для вызова
        List<Item> items = Arrays.asList(item1, item2);

        // Вызываем метод
        List<Booking> result = bookingService.getBookingsForItems(items);

        // Проверка - убедимся, что возвращаются все бронирования
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Booking::getItemId)
                .containsExactlyInAnyOrder(item1.getId(), item2.getId());
    }

    @Test
    void testGetBooking_Success_UserIsBookerOrOwner() {
        // Создаем бронирование от арендатора
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(bookerId);
        booking.setStart(NOW.plusDays(1));
        booking.setEnd(NOW.plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);

        // Сохраняем бронирование
        bookingService.addBooking(booking);
        Long bookingId = booking.getId();

        // Вызов от пользователя-арендатора (Booker)
        BookingDto resultDtoBooker = bookingService.getBooking(bookerId, bookingId);

        // Вызов от владельца вещи
        BookingDto resultDtoOwner = bookingService.getBooking(userId, bookingId);

        // Проверка, что возвращается корректная информация
        assertThat(resultDtoBooker).isNotNull();
        assertThat(resultDtoBooker.getId()).isEqualTo(bookingId);
        assertThat(resultDtoBooker.getItem().getId()).isEqualTo(itemId);
        assertThat(resultDtoOwner).isNotNull();
    }

    @Test
    void testGetBooking_UserWithoutRights_ShouldThrowValidationException() {
        User otherUser = new User();
        otherUser.setName("OtherUser");
        otherUser.setEmail("other@example.com");
        otherUser.setId(userService.addUser(UserMapper.toUserDto(otherUser)).getId());
        Long otherUserId = otherUser.getId();

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setItemId(itemId);
        booking.setItem(item);
        booking.setBookerId(userId); // Бронирование сделано владельцем
        booking.setStart(NOW.plusDays(1));
        booking.setEnd(NOW.plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);
        bookingService.addBooking(booking);
        Long bookingId = booking.getId();

        // Попытка получить бронирование от пользователя, который не является владельцем и не автором (ожидается исключение)
        assertThatThrownBy(() -> {
            bookingService.getBooking(otherUserId, bookingId);
        }).isInstanceOf(ValidationException.class)
                .hasMessage("У вас нет прав на текущее бронирование");
    }

    @Test
    void testGetBooking_NotFound_ShouldThrowNotFoundException() {
        Long nonexistentBookingId = 999L; // Нек existing booking ID

        assertThatThrownBy(() -> {
            bookingService.getBooking(userId, nonexistentBookingId);
        }).isInstanceOf(NotFoundException.class)
                .hasMessage("Бронирование не найдено");
    }
}