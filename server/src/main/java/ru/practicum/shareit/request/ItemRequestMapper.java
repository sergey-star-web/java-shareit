package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(RequestDto requestDto, Long userId) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestorId(userId);
        itemRequest.setDescription(requestDto.getDescription());
        return itemRequest;
    }

    public static ItemsRequestDto toItemsRequestDto(ItemRequest itemRequest) {
        ItemsRequestDto itemsRequestDto = new ItemsRequestDto();
        itemsRequestDto.setId(itemRequest.getId());
        itemsRequestDto.setDescription(itemRequest.getDescription());
        itemsRequestDto.setCreated(itemRequest.getCreated());
        List<ItemForRequestDto> items = itemRequest.getItems().stream()
                .map(item -> new ItemForRequestDto(item.getId(), item.getName(), item.getOwnerId()))
                .collect(Collectors.toList());
        itemsRequestDto.setItems(items);
        return itemsRequestDto;
    }
}