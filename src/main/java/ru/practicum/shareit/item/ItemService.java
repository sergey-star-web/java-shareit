package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(Integer userId);

    ItemDto getItem(Integer userId, Integer itemId);

    ItemDto addItem(Integer userId, ItemDto itemDto);

    void updateItem(Integer userId, ItemDto itemDto);
}