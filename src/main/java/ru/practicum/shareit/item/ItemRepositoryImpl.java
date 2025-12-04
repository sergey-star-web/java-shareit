package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private HashMap<Integer, List<Item>> items = new HashMap<>();
    private Integer idCounter = 1;

    private Integer genNextId() {
        return idCounter++;
    }

    @Override
    public List<Item> findByUserId(Integer userId) {
        return items.get(userId);
    }

    @Override
    public Item findByItemId(Integer userId, Integer itemId) {
        return items.get(userId).get(itemId);
    }

    @Override
    public Item save(Integer userId, Item item) {
        log.info("Получен запрос на создание вещи: {}", item);
        item.setId(genNextId());
        item.setOwner(userId);

        // Получаем текущий список для пользователя или создаем новый, если его еще нет
        List<Item> userItems = items.get(userId);
        if (userItems == null) {
            userItems = new ArrayList<>();
            items.put(userId, userItems);
        }

        // Добавляем новый элемент в список
        userItems.add(item);

        log.info("Вещь успешно создана. Созданная вещь: {}", item);
        return item;
    }

    private void throwIfNoFilm(Integer id) {
        if (!items.containsKey(id)) {
            String errorMessage = String.format("Не найдена вещь с %d", id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public Item update(Integer userId, Item item) {
        Integer id = item.getId();
        if (id != null) {
            throwIfNoFilm(id);
        }
        items.put(userId, items.get(userId));
        log.info("Вещь успешно обновлена. Измененная вещь: {}", item);
        return item;
    }
}
