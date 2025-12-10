package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Map;

public interface ItemRepository {
    Map<Integer, Item> findByUserId(Integer userId);

    Item findByItemId(Integer userId, Integer itemId);

    void save(Integer userId, Map<Integer, Item> userItems);

    void update(Integer userId, Map<Integer, Item> userItems);
}