package ru.practicum.shareit.item;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwnerId(),
                item.getRequestId()
        );
    }

    public static ItemCommDto toItemCommDto(Item item) {
        ItemCommDto itemCommDto = new ItemCommDto();
        itemCommDto.setId(item.getId());
        itemCommDto.setName(item.getName());
        itemCommDto.setDescription(item.getDescription());
        itemCommDto.setAvailable(item.getAvailable());
        return itemCommDto;
    }

    public static List<ItemDto> toItemsDto(Iterable<Item> items) {
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(toItemDto(item));
        }
        return result;
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        item.setOwnerId(itemDto.getOwnerId());
        return item;
    }
}