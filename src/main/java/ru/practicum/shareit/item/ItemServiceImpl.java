package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemListDto;
import ru.practicum.shareit.item.model.BookingDate;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    @Autowired
    private BookingService bookingService;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private static final String NOT_FOUND_ITEM_MESSAGE = "Вещь не найдена";

    @Override
    public ItemDto getItem(Long itemId) {
        return repository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElse(null);
    }

    @Override
    public ItemCommDto getItemById(Long itemId) {
        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM_MESSAGE));
        ItemCommDto itemCommDto = ItemMapper.toItemCommDto(item);
        List<Booking> bookings = bookingService.getItemBookings(item.getId());
        BookingDate bookingDate = setBookingDates(bookings);
        itemCommDto.setLastBooking(bookingDate.getLastBookingDate());
        itemCommDto.setNextBooking(bookingDate.getNextBookingDate());
        itemCommDto.setComments(getCommentsByItemId(itemId));
        return itemCommDto;
    }

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи: {}", itemDto);
        itemDto.setOwnerId(userId);
        validateItem(itemDto, userId, null);
        Item itemSave = ItemMapper.toItem(itemDto);
        Long id = repository.save(itemSave).getId();
        itemDto.setId(id);
        log.info("Вещь успешно создана. Созданная вещь: {}", itemDto);
        return itemDto;
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи: {}", itemDto);
        Item itemFromRepos = repository.findById(itemId).orElse(null);
        if (itemFromRepos == null) {
            log.warn(NOT_FOUND_ITEM_MESSAGE);
            throw new NotFoundException(NOT_FOUND_ITEM_MESSAGE);
        }
        itemDto = setItemFields(itemFromRepos, itemDto);
        ItemDto itemDtoValid = validateItem(itemDto, userId, itemFromRepos);
        Item itemUpdate = getFullItem(itemDto);
        repository.save(itemUpdate);
        log.info("Вещь успешно обновлена. Измененная вещь: {}", itemDtoValid);
        return itemDto;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        log.info("Получен запрос на добавление отзыва: {}", commentDto);
        // Проверка, что пользователь действительно брал вещь в аренду
        boolean userHasRentedItem = checkIfUserRentedItem(userId, itemId);
        if (!userHasRentedItem) {
            throw new ValidationException("Пользователь не может оставить комментарий, так как не брал вещь в аренду");
        }

        commentDto.setItem(repository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ITEM_MESSAGE)));
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setAuthorName(userService.getUser(userId).getName());
        Long id = commentRepository.save(CommentMapper.toComment(commentDto, userId)).getId();
        commentDto.setId(id);

        log.info("Отзыв успешно добавлен. Добавленный отзыв: {}", commentDto);
        return commentDto;
    }

    @Override
    public List<ItemDto> getAvailableItems(String searchText, Long userId) {
        List<Item> result = new ArrayList<>();
        String lowerCaseSearchText = searchText.toLowerCase();
        for (Item item : repository.findByOwnerId(userId)) {
            if (item.getAvailable() &&
                    (item.getName().toLowerCase().contains(lowerCaseSearchText) ||
                            item.getDescription().toLowerCase().contains(lowerCaseSearchText))) {
                result.add(item);
            }
        }
        return ItemMapper.toItemsDto(result);
    }

    @Override
    public List<ItemListDto> getItemsWithBookingDates(Long ownerId) {
        List<Item> items = repository.findByOwnerId(ownerId);

        // Получаем все бронирования для элементов данного владельца
        List<Booking> allBookings = bookingService.getBookingsForItems(items);

        Map<Long, List<Booking>> bookingsByItemId = new HashMap<>();
        for (Booking booking : allBookings) {
            bookingsByItemId.computeIfAbsent(booking.getItemId(), k -> new ArrayList<>()).add(booking);
        }

        List<ItemListDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());
            BookingDate bookingDate = setBookingDates(itemBookings);
            ItemListDto itemDto = new ItemListDto(item.getId(), item.getName(), bookingDate.getLastBookingDate(), bookingDate.getNextBookingDate());

            itemDtos.add(itemDto);
        }

        return itemDtos;
    }

    @Override
    public Boolean checkIfUserRentedItem(Long userId, Long itemId) {
        // Получаем все бронирования текущего пользователя
        List<Booking> userBookings = bookingService.getBookerBookings(userId);

        // Проверяем, есть ли среди бронирований пользователя то, которое относится к нужной вещи
        // и срок аренды которого уже закончился
        return userBookings.stream()
                .anyMatch(booking -> booking.getItemId().equals(itemId) && booking.getEnd().isBefore(LocalDateTime.now()));
    }

    private BookingDate setBookingDates(List<Booking> bookings) {
        BookingDate bookingDate = new BookingDate();
        LocalDateTime lastBookingDate = null;
        LocalDateTime nextBookingDate = null;

        if (!bookings.isEmpty()) {
            lastBookingDate = bookings.stream()
                    .max(Comparator.comparing(Booking::getEnd))
                    .get()
                    .getEnd();

            nextBookingDate = bookings.stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(Booking::getStart)
                    .orElse(null);
        }
        if (nextBookingDate == null) {
            lastBookingDate = null;
        }
        bookingDate.setLastBookingDate(lastBookingDate);
        bookingDate.setNextBookingDate(nextBookingDate);
        return bookingDate;
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentDtos = new ArrayList<>();
        List<UserDto> userDtos = userService.getUsers();
        HashMap<Long, String> users = new HashMap<>();

        for (UserDto userDto : userDtos) {
            users.put(userDto.getId(), userDto.getName());
        }
        for (Comment comment : comments) {
            CommentDto commentDto = CommentDto.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .created(comment.getCreated())
                    .authorName(users.get(comment.getAuthorId()))
                    .build();
            commentDtos.add(commentDto);
        }
        return commentDtos;
    }

    private ItemDto setItemFields(Item itemFromRepos, ItemDto itemDtoUpdate) {
        if (itemFromRepos != null) {
            if (itemDtoUpdate.getName() != null) {
                itemFromRepos.setName(itemDtoUpdate.getName());
            }
            if (itemDtoUpdate.getDescription() != null) {
                itemFromRepos.setDescription(itemDtoUpdate.getDescription());
            }
            if (itemDtoUpdate.getAvailable() != null) {
                itemFromRepos.setAvailable(itemDtoUpdate.getAvailable());
            }
            return ItemMapper.toItemDto(itemFromRepos);
        }
        return null;
    }

    private ItemDto validateItem(ItemDto itemDto, Long userId, Item itemFromRepos) {
        if (itemDto == null) {
            log.warn(NOT_FOUND_ITEM_MESSAGE);
            throw new NotFoundException(NOT_FOUND_ITEM_MESSAGE);
        }
        //Если вещь уже существует, значит происходит update
        if (itemFromRepos != null) {
            if (!userId.equals(itemFromRepos.getOwnerId())) {
                throw new NotFoundException("Редактировать вещь может только владелец. id пользователя: "
                        + userId + " id владельца вещи: " + itemFromRepos.getOwnerId());
            }
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус о доступности вещи не может быть пустым");
        } else if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new ValidationException("Наименование вещи не может быть пустым");
        } else if (itemDto.getDescription() == null) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (!userService.existsUser(userId)) {
            String errorMessage = String.format("Не найден пользователь с ID %d", userId);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return itemDto;
    }

    private Item getFullItem(ItemDto itemDto) {
        Item fullItem = ItemMapper.toItem(itemDto);
        fullItem.setId(itemDto.getId());
        fullItem.setOwnerId(itemDto.getOwnerId());
        return fullItem;
    }
}