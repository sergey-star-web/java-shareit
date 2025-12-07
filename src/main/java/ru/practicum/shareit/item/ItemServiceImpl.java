package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    @Override
    public List<ItemDto> getItems(Integer userId) {
        List<Item> items = repository.findByUserId(userId);
        return ItemMapper.toItemsDto(items);
    }

    @Override
    public ItemDto getItem(Integer userId, Integer itemId) {
        Item item = repository.findByItemId(userId, itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto addItem(Integer userId, ItemDto itemDto) {
        Item item = repository.save(userId, ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        Item item = repository.update(userId, itemId, ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAvailableItems(String searchText, Integer userId) {
        List<Item> items = repository.findAvailableItems(searchText, userId);
        return ItemMapper.toItemsDto(items);
    }
}
