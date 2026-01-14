package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCommDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemListDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController controller;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Вспомогательные методы для создания DTO
    private ItemDto createItemDto(Long id, String name) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    private ItemCommDto createItemCommDto(Long id, String name) {
        ItemCommDto dto = new ItemCommDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    private ItemListDto createItemListDto(Long id, String name) {
        ItemListDto dto = new ItemListDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    private CommentDto createCommentDto(Long id, String text) {
        CommentDto dto = new CommentDto();
        dto.setId(id);
        dto.setText(text);
        return dto;
    }

    @Test
    void testGetItemById() throws Exception {
        Long itemId = 1L;
        ItemCommDto item = createItemCommDto(itemId, "Test Item");
        when(itemService.getItemById(itemId)).thenReturn(item);

        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService).getItemById(itemId);
    }

    @Test
    void testGetItems() throws Exception {
        Long userId = 42L;
        List<ItemListDto> list = Arrays.asList(
                createItemListDto(1L, "Item1"),
                createItemListDto(2L, "Item2")
        );
        when(itemService.getItemsWithBookingDates(userId)).thenReturn(list);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("Item2"));

        verify(itemService).getItemsWithBookingDates(userId);
    }

    @Test
    void testAddItem() throws Exception {
        Long userId = 10L;
        ItemDto newItem = createItemDto(null, "NewItem");
        ItemDto savedItem = createItemDto(99L, "NewItem");
        when(itemService.addItem(eq(userId), any(ItemDto.class))).thenReturn(savedItem);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.name").value("NewItem"));

        verify(itemService).addItem(eq(userId), any(ItemDto.class));
    }

    @Test
    void testUpdateItem() throws Exception {
        Long userId = 5L;
        Long itemId = 7L;
        ItemDto updateDto = createItemDto(null, "UpdatedName");
        ItemDto updatedDto = createItemDto(itemId, "UpdatedName");
        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class))).thenReturn(updatedDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("UpdatedName"));

        verify(itemService).updateItem(eq(userId), eq(itemId), any(ItemDto.class));
    }

    @Test
    void testSearchItems() throws Exception {
        Long userId = 3L;
        String searchText = "searchTerm";
        List<ItemDto> results = Arrays.asList(
                createItemDto(1L, "ItemA"),
                createItemDto(2L, "ItemB")
        );
        when(itemService.getAvailableItems(searchText, userId)).thenReturn(results);

        mvc.perform(get("/items/search")
                        .param("text", searchText)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("ItemB"));

        verify(itemService).getAvailableItems(searchText, userId);
    }

    @Test
    void testAddComment() throws Exception {
        Long itemId = 4L;
        Long userId = 8L;
        CommentDto comment = createCommentDto(null, "Great item");
        CommentDto savedComment = createCommentDto(100L, "Great item");
        when(itemService.addComment(eq(itemId), any(CommentDto.class), eq(userId))).thenReturn(savedComment);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Great item"));

        verify(itemService).addComment(eq(itemId), any(CommentDto.class), eq(userId));
    }
}