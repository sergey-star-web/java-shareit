package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(RequestDto requestDto, Long userId) {
        log.info("Получен запрос на добавление запроса вещи: " + requestDto + " поль-ель: " + userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto, userId);
        itemRequest.setCreated(LocalDate.now());
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос вещи успешно добавлен: " + savedRequest + " поль-ель: " + userId);
        return new ItemRequestDto(savedRequest.getId(), savedRequest.getDescription(), savedRequest.getCreated());
    }

    @Override
    public List<ItemsRequestDto> getUserRequests(Long userId) {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return itemRequests.stream()
                .map(ItemRequestMapper::toItemsRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        return itemRequestRepository.findAllByOrderByCreatedDesc()
                .stream()
                .map(itemRequest -> new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
                        itemRequest.getCreated()))
                .collect(Collectors.toList());
    }

    @Override
    public ItemsRequestDto getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findAllByRequestIdOrderByCreatedDesc(requestId);
        if (itemRequest != null) {
            return ItemRequestMapper.toItemsRequestDto(itemRequest);
        } else {
            return null;
        }
    }
}