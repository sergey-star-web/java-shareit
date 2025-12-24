package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final String notFoundItemMessage = "Вещь не найдена";

    @Override
    public List<ItemDto> getItems(Long userId) {
        List<Item> items = repository.findByOwnerId(userId);
        return ItemMapper.toItemsDto(items);
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        return repository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElse(null);
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
        throwIfNoItem(itemFromRepos);
        itemDto = setItemFields(itemFromRepos, itemDto);
        ItemDto itemDtoValid = validateItem(itemDto, userId, itemFromRepos);
        Item itemUpdate = getFullItem(itemDto);
        repository.save(itemUpdate);
        log.info("Вещь успешно обновлена. Измененная вещь: {}", itemDtoValid);
        return itemDto;
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

    private void throwIfNoItem(Item item) {
        if (item == null) {
            log.warn(notFoundItemMessage);
            throw new NotFoundException(notFoundItemMessage);
        }
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
            log.warn(notFoundItemMessage);
            throw new NotFoundException(notFoundItemMessage);
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
        if (!repository.isUserExist(userId)) {
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