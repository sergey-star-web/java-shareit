package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Get items for userId={}", userId);
        return itemClient.getItems(userId);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestBody @Valid ItemDto itemDto) {
        log.info("Add item {}, userId={}", itemDto, userId);
        if (itemDto.getAvailable() == null) {
            log.error("Статус о доступности вещи не может быть пустым");
            throw new ValidationException("Статус о доступности вещи не может быть пустым");
        }
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            log.error("Наименование вещи не может быть пустым");
            throw new ValidationException("Наименование вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null) {
            log.error("Описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        return itemClient.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Update item {}, userId={}", itemId, userId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Search items with text '{}', userId={}", text, userId);
        return itemClient.searchItems(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId,
                                             @RequestBody @Valid CommentDto commentDto) {
        log.info("Add comment to item {}, userId={}", itemId, userId);
        return itemClient.addComment(itemId, commentDto, userId);
    }
}