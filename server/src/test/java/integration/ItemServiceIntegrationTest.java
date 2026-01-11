package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemListDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Sql(
        scripts = "/schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(classes = ShareItServer.class)
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

    @Test
    public void testUpdateItem_Success() {
        // Создаем пользователя-владельца
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());

        // Создаем вещь
        Item item = Item.builder()
                .name("Old Name")
                .description("Old Description")
                .available(true)
                .ownerId(owner.getId())
                .build();
        item.setId(itemService.addItem(owner.getId(), ItemMapper.toItemDto(item)).getId());

        // Пользователь, который будет обновлять вещь (тот же владелец)
        Long userId = owner.getId();

        // Новый DTO для обновления
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        // Вызов метода обновления
        ItemDto updatedItem = itemService.updateItem(userId, item.getId(), updateDto);

        // Проверки
        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getName()).isEqualTo("New Name");
        assertThat(updatedItem.getDescription()).isEqualTo("New Description");
        assertThat(updatedItem.getAvailable()).isFalse();

        // После вызова updateItem
        ItemDto updatedItemFromRepo = itemService.getItem(item.getId());

        // Проверяем, что поля совпадают с обновленными данными
        assertThat(updatedItemFromRepo.getName()).isEqualTo("New Name");
        assertThat(updatedItemFromRepo.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void testGetAvailableItems_WithSearchText_ReturnsMatchingItems() {
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // Создаем доступные и недоступные предметы для этого пользователя
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Camera");
        item1.setDescription("A digital camera");
        item1.setAvailable(true);
        item1.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item1));

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Tripod");
        item2.setDescription("A camera tripod");
        item2.setAvailable(true);
        item2.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item2));

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Lens");
        item3.setDescription("Camera lens");
        item3.setAvailable(false); // Недоступен
        item3.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item3));

        String searchText = "camera";

        // Вызов метода
        List<ItemDto> result = itemService.getAvailableItems(searchText, userId);

        // Проверки
        // Ожидаются только items, связанные с "camera" и доступны
        assertThat(result).hasSize(2);
        List<String> itemNames = result.stream()
                .map(ItemDto::getName)
                .collect(Collectors.toList());
        assertThat(itemNames).containsExactlyInAnyOrder("Camera", "Tripod");
    }

    @Test
    public void testGetAvailableItems_NoMatches_ReturnsEmpty() {
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // Создаем доступные и недоступные предметы для этого пользователя
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Camera");
        item1.setDescription("A digital camera");
        item1.setAvailable(true);
        item1.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item1));

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Tripod");
        item2.setDescription("A camera tripod");
        item2.setAvailable(true);
        item2.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item2));

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Lens");
        item3.setDescription("Camera lens");
        item3.setAvailable(false); // Недоступен
        item3.setOwnerId(userId);
        itemService.addItem(userId, ItemMapper.toItemDto(item3));

        String searchText = "nonexistent";

        List<ItemDto> result = itemService.getAvailableItems(searchText, userId);

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetItemById_FullFlow() {
        // Создаем пользователя-владельца
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner");
        ownerDto.setEmail("owner@example.com");
        ownerDto.setId(userService.addUser(ownerDto).getId()); // сохраняем пользователя и получаем его ID
        Long ownerId = ownerDto.getId();

        // Создаем пользователя-владельца
        UserDto rentedDto = new UserDto();
        rentedDto.setName("Rented");
        rentedDto.setEmail("rented@example.com");
        rentedDto.setId(userService.addUser(rentedDto).getId()); // сохраняем пользователя и получаем его ID
        Long rentedId = rentedDto.getId();

        // Создаем вещь
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Camera");
        item1.setDescription("A digital camera");
        item1.setAvailable(true);
        item1.setOwnerId(ownerId);
        item1.setId(itemService.addItem(ownerId, ItemMapper.toItemDto(item1)).getId());
        Long itemId = item1.getId();

        // Создаем бронирования через сервис
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime startBooking = now.minusDays(5);
        LocalDateTime endBooking = now.minusDays(2);

        // добавляем бронирование — предполагается, что так работает ваш сервис
        Booking booking = Booking.builder()
                .start(startBooking)
                .end(endBooking)
                .itemId(itemId)
                .build();
        BookingDto bookingDto = bookingService.addBooking(rentedId, BookingMapper.toBookingRequestDto(booking));

        itemService.addComment(itemId, new CommentDto(null, "Comment 1", item1,
                "Owner", LocalDateTime.now()), rentedId);
        itemService.addComment(itemId, new CommentDto(null, "Comment 2", item1,
                "Owner", LocalDateTime.now().plusDays(2)), rentedId);

        // Вызов целевого метода
        ItemCommDto result = itemService.getItemById(itemId);

        // Проверки
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Comment 1");
        assertThat(bookingDto.getEnd()).isEqualTo(endBooking);
        assertThat(bookingDto.getStart()).isEqualTo(startBooking);
    }

    @Test
    public void testValidateItem_NullItemDto_ThrowsNotFoundException() {
        // Создаем пользователя
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // Вызов с null itemDto
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            itemService.validateItem(null, userId, null);
        });
        assertEquals("Вещь не найдена", thrown.getMessage());
    }

    @Test
    public void testValidateItem_UserNotExist_ThrowsNotFoundException() {
        Long userId = 999L; // Пользователя с таким ID нет в системе
        // Создаем dummy itemDto
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Camera");
        itemDto.setDescription("A digital camera");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(userId);

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            itemService.validateItem(itemDto, userId, null);
        });
        assertEquals("Не найден пользователь с ID 999", thrown.getMessage());
    }

    @Test
    public void testValidateItem_ItemFromReposWithDifferentOwner_ThrowsNotFoundException() {
        // Создаем пользователя
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // Создаем вещь с другим владельцем
        Item itemFromRepos = new Item();
        itemFromRepos.setId(1L);
        itemFromRepos.setOwnerId(userId + 1); // другой владелец

        // Создаем DTO
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Camera");
        itemDto.setDescription("A digital camera");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(userId);

        // Вызов, ожидаемый выброс исключения
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            itemService.validateItem(itemDto, userId, itemFromRepos);
        });
        assertTrue(thrown.getMessage().contains("Редактировать вещь может только владелец"));
    }

    @Test
    public void testValidateItem_ItemAvailableNull_ThrowsValidationException() {
        // Создаем пользователя
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // Создаем DTO с null доступностью
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Camera");
        itemDto.setDescription("A digital camera");
        itemDto.setAvailable(null);
        itemDto.setOwnerId(userId);

        // Создаем вещь владельца
        Item itemFromRepos = new Item();
        itemFromRepos.setId(1L);
        itemFromRepos.setOwnerId(userId);
        itemFromRepos.setAvailable(true);

        // Проверка
        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            itemService.validateItem(itemDto, userId, itemFromRepos);
        });
        assertEquals("Статус о доступности вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    public void testValidateItem_ItemNameEmpty_ThrowsValidationException() {
        // Создаем пользователя
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // DTO с пустым именем
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("");
        itemDto.setDescription("A digital camera");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(userId);

        // Вещь владельца
        Item itemFromRepos = new Item();
        itemFromRepos.setId(1L);
        itemFromRepos.setOwnerId(userId);
        itemFromRepos.setAvailable(true);

        // Тест
        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            itemService.validateItem(itemDto, userId, itemFromRepos);
        });
        assertEquals("Наименование вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    public void testValidateItem_ItemDescriptionNull_ThrowsValidationException() {
        // Создаем пользователя
        User owner = User.builder()
                .name("ownerName")
                .email("owner@example.com")
                .build();
        owner.setId(userService.addUser(UserMapper.toUserDto(owner)).getId());
        Long userId = owner.getId();

        // DTO с null description
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Camera");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(userId);

        // Вещь владельца
        Item itemFromRepos = new Item();
        itemFromRepos.setId(1L);
        itemFromRepos.setOwnerId(userId);
        itemFromRepos.setAvailable(true);

        // Тест
        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            itemService.validateItem(itemDto, userId, itemFromRepos);
        });
        assertEquals("Описание вещи не может быть пустым", thrown.getMessage());
    }
}