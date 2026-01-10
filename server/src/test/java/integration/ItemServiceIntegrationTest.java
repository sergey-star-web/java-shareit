package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.*;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import config.AppConfig;
import config.PersistenceConfig;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemListDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Transactional
@SpringBootTest
@SpringJUnitConfig({UserServiceImpl.class, BookingServiceImpl.class, ItemServiceImpl.class, AppConfig.class,
        PersistenceConfig.class})
public class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;

    @Test
    public void testGetItemsWithBookingDates() {
        // Создаем пользователя-владельца вещи
        User user = User.builder().id(1L).name("testuser1").email("owner@example.com").build();
        user.setId(userService.addUser(UserMapper.toUserDto(user)).getId());

        // Создаем вещи
        Item item1 = Item.builder().name("Item 1").description("Description 1").available(true).ownerId(user.getId()).build();
        Item item2 = Item.builder().name("Item 2").description("Description 2").available(true).ownerId(user.getId()).build();

        item1.setId(itemService.addItem(user.getId(), ItemMapper.toItemDto(item1)).getId());
        item2.setId(itemService.addItem(user.getId(), ItemMapper.toItemDto(item2)).getId());

        // Текущее время, обрезанное до секунд
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Создаем бронирования с определенными датами
        LocalDateTime bookingStart1 = now.minusDays(5);
        LocalDateTime bookingEnd1 = now.minusDays(2);

        LocalDateTime bookingStart2 = now.plusDays(1);
        LocalDateTime bookingEnd2 = now.plusDays(3);

        LocalDateTime bookingStart3 = now.plusDays(4);
        LocalDateTime bookingEnd3 = now.plusDays(6);

        // Создаем бронирования
        Booking booking1 = Booking.builder()
                .start(bookingStart1)
                .end(bookingEnd1)
                .itemId(item1.getId())
                .status(BookingStatus.APPROVED)
                .build();

        Booking booking2 = Booking.builder()
                .start(bookingStart2)
                .end(bookingEnd2)
                .itemId(item1.getId())
                .status(BookingStatus.WAITING)
                .build();

        Booking booking3 = Booking.builder()
                .start(bookingStart3)
                .end(bookingEnd3)
                .itemId(item2.getId())
                .status(BookingStatus.APPROVED)
                .build();

        // Добавляем бронирования
        bookingService.addBooking(user.getId(), BookingMapper.toBookingRequestDto(booking1));
        bookingService.addBooking(user.getId(), BookingMapper.toBookingRequestDto(booking2));
        bookingService.addBooking(user.getId(), BookingMapper.toBookingRequestDto(booking3));

        // Вызов тестируемого метода
        List<ItemListDto> result = itemService.getItemsWithBookingDates(user.getId());

        // Проверки
        assertThat(result).hasSize(2);

        for (var itemDto : result) {
            if (itemDto.getId().equals(item1.getId())) {
                assertThat(itemDto.getNextBookingDate()).isEqualTo(bookingStart2);
                assertThat(itemDto.getLastBookingDate()).isEqualTo(bookingEnd2);
            } else if (itemDto.getId().equals(item2.getId())) {
                assertThat(itemDto.getNextBookingDate()).isEqualTo(bookingStart3);
                assertThat(itemDto.getLastBookingDate()).isEqualTo(bookingEnd3);
            }
        }
    }
}