package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    List<ItemListDto> getItemsWithBookingDates(Long userId);

    ItemDto getItem(Long itemId);

    ItemCommDto getItemById(Long itemId);

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getAvailableItems(String searchText, Long userId);

    CommentDto addComment(Long itemId, CommentDto commentDto, Long userId);
}