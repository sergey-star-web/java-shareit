package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
public class BookingRepositoryIntegrationTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository; // предположим, есть репозиторий для Item
    @Autowired
    private UserRepository userRepository; // и для User, если нужно

    private Long itemId;
    private Long userId;
    private Long bookerId;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;

    @BeforeEach
    void setUp() {
        // Создание владельца
        User user = new User();
        user.setName("Владелец");
        user.setEmail("owner@example.com");
        userId = userRepository.save(user).getId();

        // Например:
        User booker = new User();
        booker.setName("John");
        booker.setEmail("John@example.com");
        bookerId = userRepository.save(booker).getId();

        Item item = new Item();
        item.setName("Предмет владельца");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwnerId(userId);
        itemId = itemRepository.save(item).getId();

        // Создайте бронирования
        booking1 = new Booking();
        booking1.setItem(item);
        booking1.setItemId(itemId);
        booking1.setBookerId(bookerId);
        booking1.setStart(NOW.minusDays(3));
        booking1.setEnd(NOW.minusDays(1));
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        booking2 = new Booking();
        booking2.setItem(item);
        booking2.setItemId(itemId);
        booking2.setBookerId(bookerId);
        booking2.setStart(NOW.plusDays(1));
        booking2.setEnd(NOW.plusDays(3));
        booking2.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking2);

        booking3 = new Booking();
        booking3.setItem(item);
        booking3.setItemId(itemId);
        booking3.setBookerId(bookerId);
        booking3.setStart(NOW.plusDays(4));
        booking3.setEnd(NOW.plusDays(6));
        booking3.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking3);
    }

    @Test
    void testFindAllByItemId() {
        List<Booking> bookings = bookingRepository.findAllByItemId(itemId);
        assertThat(bookings).hasSize(3);
    }

    @Test
    void testFindAllByItemIdAndStatusAndConflict() {
        LocalDateTime start = NOW.plusDays(0);
        LocalDateTime end = NOW.plusDays(2);
        List<Booking> result = bookingRepository.findAllByItemIdAndStatusAndConflict(
                itemId, BookingStatus.WAITING, start, end);
        assertThat(result).isNotEmpty();
    }

    @Test
    void testFindAllByBookerIdAndEndBefore() {
        // Проверка бронирований, завершившихся до текущего времени
        List<Booking> pastBookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, NOW);
        assertThat(pastBookings).hasSize(1);
        assertThat(pastBookings.get(0).getStart()).isBefore(NOW);
    }

    @Test
    void testFindAllByBookerIdAndStartAfter() {
        // Проверка будущих бронирований
        List<Booking> futureBookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, NOW);
        assertThat(futureBookings).hasSize(2);
        assertThat(futureBookings.get(0).getStart()).isAfter(NOW);
    }

    @Test
    void testFindAllByBookerIdAndStatusOrderByStartDesc() {
        // Проверка бронирований по статусу
        List<Booking> approvedBookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId,
                BookingStatus.APPROVED);
        assertThat(approvedBookings).hasSize(1);
        assertThat(approvedBookings.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void testFindAllByBookerIdOrderByStartDesc() {
        // Проверка всех бронирований по пользователю
        List<Booking> allBookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
        assertThat(allBookings).hasSize(3);
        // Проверка порядка (по убыванию start)
        assertThat(allBookings.get(0).getStart()).isAfterOrEqualTo(allBookings.get(1).getStart());
    }

    @Test
    void testFindAllByItemIdIn() {
        // Проверка поиска по списку itemId
        List<Long> itemIds = Arrays.asList(itemId);
        List<Booking> bookings = bookingRepository.findAllByItemIdIn(itemIds);
        assertThat(bookings).hasSize(3);
    }

    @Test
    void testSaveBooking() {
        // Создаем нового пользователя
        User newUser = new User();
        newUser.setName("Новый User");
        newUser.setEmail("newuser@example.com");
        Long newUserId = userRepository.save(newUser).getId();

        // Создаем нового предмета
        Item newItem = new Item();
        newItem.setName("Новый предмет");
        newItem.setDescription("Описание");
        newItem.setAvailable(true);
        newItem.setOwnerId(newUserId);
        Long newItemId = itemRepository.save(newItem).getId();

        // Создаем бронирование
        Booking newBooking = new Booking();
        newBooking.setItem(newItem);
        newBooking.setItemId(newItemId);
        newBooking.setBookerId(newUserId);
        newBooking.setStart(NOW.plusDays(10));
        newBooking.setEnd(NOW.plusDays(12));
        newBooking.setStatus(BookingStatus.WAITING);

        // Сохраняем бронирование
        Booking savedBooking = bookingRepository.save(newBooking);
        Long bookingId = savedBooking.getId();

        // Получаем из базы по ID
        Booking fetchedBooking = bookingRepository.findById(bookingId).orElse(null);

        // Проверяем, что сохранено корректно
        assertThat(fetchedBooking).isNotNull();
        assertThat(fetchedBooking.getItemId()).isEqualTo(newItemId);
        assertThat(fetchedBooking.getBookerId()).isEqualTo(newUserId);
        String startTruncated = fetchedBooking.getStart().format(FORMATTER);
        String endTruncated = fetchedBooking.getEnd().format(FORMATTER);
        assertThat(startTruncated).isEqualTo(NOW.plusDays(10).format(FORMATTER));
        assertThat(endTruncated).isEqualTo(NOW.plusDays(12).format(FORMATTER));
        assertThat(fetchedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}