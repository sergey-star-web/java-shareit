package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.user.UserRepository;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private HashMap<Integer, List<Item>> items = new HashMap<>();
    private Integer idCounter = 1;
    private final UserRepository userRepository;

    private Integer genNextId() {
        return idCounter++;
    }

    @Override
    public List<Item> findByUserId(Integer userId) {
        return items.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public Item findByItemId(Integer userId, Integer itemId) {
        List<Item> itemList = findByUserId(userId);
        for (Item item : itemList) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        throw new NotFoundException("Вещи пользователя отсутствуют");
    }

    @Override
    public Item save(Integer userId, Item item) {
        log.info("Получен запрос на создание вещи: {}", item);
        validateItem(item, userId);
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

    private void throwIfNoItem(Integer id) {
        if (!items.containsKey(id)) {
            String errorMessage = String.format("Не найдена вещь с %d", id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private void validateItem(Item item, Integer userId) {
        if (item != null) {
            if (item.getAvailable() == null) {
                throw new ValidationException("Статус о доступности вещи не может быть пустым");
            } else if (item.getName() == null || item.getName().isEmpty()) {
                throw new ValidationException("Наименование вещи не может быть пустым");
            } else if (item.getDescription() == null) {
                throw new ValidationException("Описание вещи не может быть пустым");
            }
            if (userRepository.findByUserId(userId) == null){
                String errorMessage = String.format("Не найден пользователь с ID %d", userId);
                log.warn(errorMessage);
                throw new NotFoundException(errorMessage);
            }
        } else {
            String errorMessage = "Вещь не найдена";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public Item update(Integer userId, Integer itemId, Item updateItem) {
        log.info("Получен запрос на обновление вещи: {}", updateItem);
        Integer id = updateItem.getId();
        if (id != null) {
            throwIfNoItem(id);
        }
        Item item = findByItemId(userId, itemId);
        if (updateItem.getName() != null) {
            item.setName(updateItem.getName());
        }
        if (updateItem.getDescription() != null) {
            item.setDescription(updateItem.getDescription());
        }
        if (updateItem.getAvailable() != null) {
            item.setAvailable(updateItem.getAvailable());
        }
        validateItem(item, userId);
        List<Item> userItems = items.get(userId);
        if (userItems == null) {
            userItems = new ArrayList<>();
            items.put(userId, userItems);
        }
        userItems.removeIf(i -> i.getId().equals(itemId)); // Удаляем старый элемент
        userItems.add(item); // Добавляем обновлённый элемент
        items.put(userId, userItems);
        log.info("Вещь успешно обновлена. Измененная вещь: {}", updateItem);
        return updateItem;
    }

    @Override
    public List<Item> findAvailableItems(String searchText, Integer userId) {
        List<Item> result = new ArrayList<>();
        String lowerCaseSearchText = searchText.toLowerCase();
        for (Item item : items.get(userId)) {
            if (item.getAvailable() &&
                    (item.getName().toLowerCase().contains(lowerCaseSearchText) ||
                            item.getDescription().toLowerCase().contains(lowerCaseSearchText))) {
                result.add(item);
            }
        }
        return result;
    }
}