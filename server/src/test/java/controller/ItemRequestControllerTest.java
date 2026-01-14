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

import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    private MockMvc mvc;
    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Вспомогательный метод для создания RequestDto
    private RequestDto createRequestDto(String description) {
        RequestDto dto = new RequestDto();
        dto.setDescription(description);
        return dto;
    }

    // Вспомогательный метод для создания ItemRequestDto
    private ItemRequestDto createItemRequestDto(Long id, String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        return dto;
    }

    // Вспомогательный метод для создания ItemsRequestDto
    private ItemsRequestDto createItemsRequestDto(Long id, String description) {
        ItemsRequestDto dto = new ItemsRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        return dto;
    }

    @Test
    void createRequestTest() throws Exception {
        Long userId = 1L;
        RequestDto requestDto = createRequestDto("Test request");
        ItemRequestDto createdDto = createItemRequestDto(1L, "Test request");

        when(itemRequestService.createRequest(any(RequestDto.class), eq(userId))).thenReturn(createdDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdDto.getId()))
                .andExpect(jsonPath("$.description").value(createdDto.getDescription()));

        verify(itemRequestService).createRequest(any(RequestDto.class), eq(userId));
    }

    @Test
    void getUserRequestsTest() throws Exception {
        Long userId = 2L;
        List<ItemsRequestDto> list = Arrays.asList(
                createItemsRequestDto(1L, "Request 1"),
                createItemsRequestDto(2L, "Request 2")
        );

        when(itemRequestService.getUserRequests(userId)).thenReturn(list);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Request 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Request 2"));

        verify(itemRequestService).getUserRequests(userId);
    }

    @Test
    void getAllRequestsTest() throws Exception {
        List<ItemRequestDto> list = Arrays.asList(
                createItemRequestDto(1L, "All Request 1"),
                createItemRequestDto(2L, "All Request 2")
        );

        when(itemRequestService.getAllRequests()).thenReturn(list);

        mvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("All Request 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("All Request 2"));

        verify(itemRequestService).getAllRequests();
    }

    @Test
    void getRequestByIdTest() throws Exception {
        Long requestId = 10L;
        ItemsRequestDto dto = createItemsRequestDto(requestId, "Request by ID");

        when(itemRequestService.getRequestById(requestId)).thenReturn(dto);

        mvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Request by ID"));

        verify(itemRequestService).getRequestById(requestId);
    }
}
