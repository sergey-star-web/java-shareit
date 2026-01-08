package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(RequestDto requestDto, Long userId);

    List<ItemsRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests();

    ItemsRequestDto getRequestById(Long requestId);
}