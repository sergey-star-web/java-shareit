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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    private MockMvc mvc;
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }
    // Пример данных
    private UserDto createSampleUserDto(Long id, String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }

    @Test
    void testAddUser() throws Exception {
        UserDto userDto = createSampleUserDto(null, "John", "john@test.com");
        UserDto savedUserDto = createSampleUserDto(1L, "John", "john@test.com");

        when(userService.addUser(any(UserDto.class))).thenReturn(savedUserDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUserDto.getId()))
                .andExpect(jsonPath("$.name").value(savedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(savedUserDto.getEmail()));

        verify(userService).addUser(any(UserDto.class));
    }

    @Test
    void testGetUser() throws Exception {
        Long userId = 1L;
        UserDto userDto = createSampleUserDto(userId, "John", "john@test.com");

        when(userService.getUser(userId)).thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService).getUser(userId);
    }

    @Test
    void testUpdateUser() throws Exception {
        Long userId = 1L;
        UserDto updateDto = createSampleUserDto(null, "Updated", "updated@test.com");
        UserDto updatedUser = createSampleUserDto(userId, "Updated", "updated@test.com");

        when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(updatedUser);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()));

        verify(userService).updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        Long userId = 1L;
        UserDto deletedUser = createSampleUserDto(userId, "John", "john@test.com");

        when(userService.deleteUser(userId)).thenReturn(deletedUser);

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(deletedUser.getName()))
                .andExpect(jsonPath("$.email").value(deletedUser.getEmail()));

        verify(userService).deleteUser(userId);
    }

}
