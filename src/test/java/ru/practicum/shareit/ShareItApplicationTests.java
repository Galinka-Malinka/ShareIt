package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ShareItApplicationTests {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUser() throws Exception {

        User user = User.builder()
                .name("User")
                .email("User@email.ru")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@email.ru"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        createUser(1);

        User updatedUser = User.builder()
                .id(1L)
                .name("UpdatedUser")
                .email("UpdatedUser@email.ru")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("UpdatedUser"))
                .andExpect(jsonPath("$.email").value("UpdatedUser@email.ru"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        createUser(1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("User1"))
                .andExpect(jsonPath("$.email").value("User1@email.ru"));
    }

    @Test
    void shouldGetUsers() throws Exception {
        createUser(1);

        User newUser1 = User.builder()
                .id(1L)
                .name("User1")
                .email("User1@email.ru")
                .build();

        createUser(2);

        User newUser2 = User.builder()
                .id(2L)
                .name("User2")
                .email("User2@email.ru")
                .build();

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(newUser1, newUser2))));
    }

    @Test
    void shouldDeleteUserById() throws Exception {
        createUser(1);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<>())));
    }

    @Test
    void shouldCreateItem() throws Exception {
        createUser(1);

        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Description for Item")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description for Item"))
                .andExpect(jsonPath("$.ownerId").value("1"))
                .andExpect(jsonPath("$.available").value("true"));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        createUser(1);
        createItem(1, 1L);

        ItemDto itemDtoForUpdating = ItemDto.builder()
                .name("UpdatedItem")
                .description("Description for updatedItem")
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(itemDtoForUpdating))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("UpdatedItem"))
                .andExpect(jsonPath("$.description").value("Description for updatedItem"))
                .andExpect(jsonPath("$.ownerId").value("1"))
                .andExpect(jsonPath("$.available").value("false"));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedItem"))
                .andExpect(jsonPath("$.description").value("Description for updatedItem"))
                .andExpect(jsonPath("$.available").value("false"));
    }

    @Test
    void shouldGetItem() throws Exception {
        createUser(1);
        createItem(1, 1L);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Item1"))
                .andExpect(jsonPath("$.description").value("Description for Item1"))
                .andExpect(jsonPath("$.available").value("true"));
    }

    @Test
    void shouldGetItemsUser() throws Exception {

        createUser(1);
        createUser(2);

        ItemDto itemDto1 = createItem(1, 1L);

        ItemDto itemDto2 = createItem(2, 1L);

        ItemDto itemDto3 = createItem(3, 2L);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(itemDto1, itemDto2))));
    }

    @Test
    void shouldGetItemsOnRequest() throws Exception {

        createUser(1); //пользователь 1
        createUser(2);  //пользователь 2

        ItemDto itemDtoUser = createItem(1, 1L);

        ItemDto itemDtoOwner1 = ItemDto.builder()  //text в 'name'
                .name("ItemOwner1 cool")
                .description("Description for ItemOwner1")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(itemDtoOwner1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        itemDtoOwner1.setId(2L);

        ItemDto itemDtoOwner2 = ItemDto.builder()  // text в 'description'
                .name("ItemOwner2")
                .description("Description for ItemOwner2 cool")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(itemDtoOwner2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        itemDtoOwner2.setId(3L);

        ItemDto itemDtoOwner3 = createItem(4, 2L);

        ItemDto itemDtoOwner4 = ItemDto.builder()  // text в 'name' и в 'description'
                .name("ItemOwner4 cool")
                .description("Description for ItemOwner4 cool")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(itemDtoOwner4))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        itemDtoOwner4.setId(5L);

        ItemDto itemDtoOwner5 = ItemDto.builder()  // не пройдёт, т.к. false
                .name("ItemOwner5 cool")
                .description("Description for ItemOwner4 cool")
                .available(false)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(itemDtoOwner5))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        itemDtoOwner5.setId(6L);

        mockMvc.perform(get("/items/search")
                        .param("text", "cool"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(Set.of(itemDtoOwner1, itemDtoOwner4, itemDtoOwner2))));
    }

    public void createUser(int number) throws Exception {
        User user = User.builder()
                .name("User" + number)
                .email("User" + number + "@email.ru")
                .build();

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    public ItemDto createItem(int number, Long userId) throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Item" + number)
                .description("Description for Item" + number)
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        itemDto.setId((long) number);
        return itemDto;
    }
}
