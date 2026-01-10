package integration;

import config.PersistenceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemsRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig({ItemRequestServiceImpl.class, UserServiceImpl.class, PersistenceConfig.class})
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;

    @Test
    void testGetUserRequests_ReturnsRequests() {
        User originalUser = new User();
        originalUser.setName("Иван");
        originalUser.setEmail("ivan@example.com");
        // сохранить пользователя (предположим, есть метод)
        UserDto savedUser = userService.addUser(UserMapper.toUserDto(originalUser));
        Long userId = savedUser.getId();

        // Пример: создаем 2 запроса
        RequestDto requestDto1 = new RequestDto();
        requestDto1.setDescription("Запрос 1");
        itemRequestService.createRequest(requestDto1, userId);

        RequestDto requestDto2 = new RequestDto();
        requestDto2.setDescription("Запрос 2");
        itemRequestService.createRequest(requestDto2, userId);

        // 1. вызываем метод получения запросов
        List<ItemsRequestDto> requests = itemRequestService.getUserRequests(userId);

        // 2. Проверяем, что список содержит созданные запросы
        assertThat(requests).isNotNull();
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting("description")
                .containsExactlyInAnyOrder("Запрос 1", "Запрос 2");
    }
}