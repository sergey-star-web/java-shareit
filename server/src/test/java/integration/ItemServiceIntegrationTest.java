package integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.*;

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
import java.util.List;

@Rollback
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = { "jdbc.url=jdbc:postgresql://localhost:5432/test"})
@SpringJUnitConfig({UserServiceImpl.class, BookingServiceImpl.class, ItemServiceImpl.class, AppConfig.class,
        PersistenceConfig.class})
public class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final BookingService bookingService;
    private final UserService userService;

    @Test
    public void testGetItemsWithBookingDates() {
        // Создаем пользователя-владельца вещи
        User user = User.builder().id(1L).name("testuser1").email("owner@example.com").build();
        user.setId(userService.addUser(UserMapper.toUserDto(user)).getId());

        // Создаем вещи
        Item item1 = Item.builder().name("Item 1").description("Description 1").available(true).ownerId(user.getId())
                .build();
        Item item2 = Item.builder().name("Item 2").description("Description 2").available(true).ownerId(user.getId())
                .build();

        item1.setId(itemService.addItem(user.getId(), ItemMapper.toItemDto(item1)).getId());
        item2.setId(itemService.addItem(user.getId(), ItemMapper.toItemDto(item2)).getId());

        // Создаем бронировщиков
        User booker1 = User.builder().name("Booker1 Name").email("booker1@example.com").build();
        booker1.setId(userService.addUser(UserMapper.toUserDto(booker1)).getId());
        User booker2 = User.builder().name("Booker2 Name").email("booker2@example.com").build();
        booker2.setId(userService.addUser(UserMapper.toUserDto(booker2)).getId());

        LocalDateTime bookingStart1 = LocalDateTime.now().minusDays(5);
        LocalDateTime bookingEnd1 = LocalDateTime.now().minusDays(2);
        LocalDateTime bookingStart2 = LocalDateTime.now().plusDays(1);
        LocalDateTime bookingEnd2 = LocalDateTime.now().plusDays(3);
        LocalDateTime bookingStart3 = LocalDateTime.now().plusDays(4);
        LocalDateTime bookingEnd3 = LocalDateTime.now().plusDays(6);

        // Создаем бронирования для вещей
        // Создаём бронирование 1
        Booking booking1 = Booking.builder()
                .start(bookingStart1)
                .end(bookingEnd1)
                .itemId(item1.getId())
                .status(BookingStatus.APPROVED)
                .build();

        // Создаём бронирование 2
        Booking booking2 = Booking.builder()
                .start(bookingStart2)
                .end(bookingEnd2)
                .itemId(item1.getId())
                .status(BookingStatus.WAITING)
                .build();

        // Создаём бронирование 3
        Booking booking3 = Booking.builder()
                .start(bookingStart3)
                .end(bookingEnd3)
                .itemId(item2.getId())
                .status(BookingStatus.APPROVED)
                .build();
        bookingService.addBooking(booker1.getId(), BookingMapper.toBookingRequestDto(booking1));
        bookingService.addBooking(booker2.getId(), BookingMapper.toBookingRequestDto(booking2));
        bookingService.addBooking(booker1.getId(), BookingMapper.toBookingRequestDto(booking3));

        // Вызов тестируемого метода
        List<ItemListDto> result = itemService.getItemsWithBookingDates(user.getId());

        // Проверки
        assertThat(result).hasSize(2);

        // Проверяем, что у каждого элемента есть корректные даты
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