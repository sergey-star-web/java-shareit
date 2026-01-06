package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemListDto;

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