package integration;

import config.AppConfig;
import config.PersistenceConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringJUnitConfig({ItemRequestServiceImpl.class, UserServiceImpl.class, AppConfig.class, PersistenceConfig.class})
public class ItemRequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    @Rollback
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
