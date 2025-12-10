package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private Integer idCounter = 1;

    @Override
    public List<ItemDto> getItems(Integer userId) {
        Map<Integer, Item> itemsMap = repository.findByUserId(userId);
        List<Item> items = new ArrayList<>(itemsMap.values());
        return ItemMapper.toItemsDto(items);
    }

    @Override
    public ItemDto getItem(Integer userId, Integer itemId) {
        Item item = repository.findByItemId(userId, itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto addItem(Integer userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи: {}", itemDto);
        validateItem(itemDto, userId);
        itemDto.setId(genNextId());
        itemDto.setOwner(userId);
        // Получаем текущий список для пользователя или создаем новый, если его еще нет
        Map<Integer, Item> userItems = repository.findByUserId(userId);
        // Добавляем новый элемент в список
        userItems.put(itemDto.getId(), getFullItem(itemDto));
        repository.save(userId, userItems);
        log.info("Вещь успешно создана. Созданная вещь: {}", itemDto);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи: {}", itemDto);
        itemDto.setId(itemId);
        ItemDto itemDtoValid = validateItem(itemDto, userId);
        Map<Integer, Item> userItems = repository.findByUserId(userId);
        userItems.remove(itemId); // Удаляем старый элемент по ключу
        userItems.put(itemDtoValid.getId(), getFullItem(itemDto)); // Добавляем обновлённый элемент
        repository.update(userId, userItems);
        log.info("Вещь успешно обновлена. Измененная вещь: {}", itemDtoValid);
        return itemDtoValid;
    }

    @Override
    public List<ItemDto> getAvailableItems(String searchText, Integer userId) {
        List<Item> result = new ArrayList<>();
        String lowerCaseSearchText = searchText.toLowerCase();
        for (Item item : repository.findByUserId(userId).values()) {
            if (item.getAvailable() &&
                    (item.getName().toLowerCase().contains(lowerCaseSearchText) ||
                            item.getDescription().toLowerCase().contains(lowerCaseSearchText))) {
                result.add(item);
            }
        }
        return ItemMapper.toItemsDto(result);
    }

    private Integer genNextId() {
        return idCounter++;
    }

    private ItemDto validateItem(ItemDto itemDto, Integer userId) {
        if (itemDto == null) {
            String errorMessage = "Вещь не найдена";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        Item getItem = repository.findByItemId(userId, itemDto.getId());
        //Если вещь уже существует, значит происходит update
        if (getItem != null) {
            if (!userId.equals(getItem.getOwner())) {
                throw new ValidationException("Редактировать вещь может только владелец. id пользователя: "
                        + userId + " id владельца вещи: " + getItem.getOwner());
            }
            if (itemDto.getName() != null) {
                getItem.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                getItem.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                getItem.setAvailable(itemDto.getAvailable());
            }
            itemDto = ItemMapper.toItemDto(getItem);
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус о доступности вещи не может быть пустым");
        } else if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            throw new ValidationException("Наименование вещи не может быть пустым");
        } else if (itemDto.getDescription() == null) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (userRepository.findByUserId(userId) == null) {
            String errorMessage = String.format("Не найден пользователь с ID %d", userId);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return itemDto;
    }

    private Item getFullItem(ItemDto itemDto) {
        Item fullItem = ItemMapper.toItem(itemDto);
        fullItem.setId(itemDto.getId());
        fullItem.setOwner(itemDto.getOwner());
        return fullItem;
    }
}