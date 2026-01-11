package integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItServer.class)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;

    @Test
    void testGetRequestById_ReturnsDtoWhenExistsAndNullWhenNotFound() {
        // Создаем пользователя
        User originalUser = new User();
        originalUser.setName("Иван");
        originalUser.setEmail("ivan@example.com");
        UserDto savedUser = userService.addUser(UserMapper.toUserDto(originalUser));
        Long userId = savedUser.getId();

        // Создаем запрос
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription("Описание запроса");
        ItemRequestDto createdRequest = itemRequestService.createRequest(requestDto, userId);
        Long existingRequestId = createdRequest.getId();

        // Проверка, что при существующем ID возвращается DTO
        ItemsRequestDto resultExist = itemRequestService.getRequestById(existingRequestId);
        assertNotNull(resultExist);
        assertEquals(existingRequestId, resultExist.getId());
        assertEquals("Описание запроса", resultExist.getDescription());

        // Проверка, что при несуществующем ID возвращается null
        Long fakeId = 999999L; // предположим, такого ID не существует
        ItemsRequestDto resultNotExist = itemRequestService.getRequestById(fakeId);
        assertNull(resultNotExist);
    }
}