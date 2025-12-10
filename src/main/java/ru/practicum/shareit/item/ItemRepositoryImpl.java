package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private Map<Integer, Map<Integer, Item>> items = new HashMap<>();

    @Override
    public Map<Integer, Item> findByUserId(Integer userId) {
        return items.getOrDefault(userId, new HashMap<>());
    }

    @Override
    public Item findByItemId(Integer userId, Integer itemId) {
        return items.getOrDefault(userId, new HashMap<>()).get(itemId);
    }

    @Override
    public void save(Integer userId, Map<Integer, Item> userItems) {
        items.put(userId, userItems);
    }

    @Override
    public void update(Integer userId, Map<Integer, Item> userItems) {
        items.put(userId, userItems);
    }
}