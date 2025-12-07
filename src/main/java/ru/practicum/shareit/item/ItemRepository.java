package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findByUserId(Integer userId);

    Item findByItemId(Integer userId, Integer itemId);

    Item save(Integer userId, Item item);

    Item update(Integer userId, Integer itemId, Item item);
}