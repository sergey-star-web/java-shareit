package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemListDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemCommDto get(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping("")
    public List<ItemListDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsWithBookingDates(userId);
    }

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @RequestBody ItemDto item) {
        return itemService.addItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Valid  @PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto item) {
        return itemService.updateItem(userId, itemId, item);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String searchText,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAvailableItems(searchText, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody @Valid CommentDto comment) {
        return itemService.addComment(itemId, comment, userId);
    }
}