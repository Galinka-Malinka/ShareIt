package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDetailedDto;
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
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.available").value("false"));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedItem"))
                .andExpect(jsonPath("$.description").value("Description for updatedItem"))
                .andExpect(jsonPath("$.available").value("false"));
    }

    @Test
    void shouldGetItem() throws Exception {
        createUser(1);
        createItem(1, 1L);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
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
        ItemDetailedDto itemDetailedDto1 = ItemDetailedDto.builder()
                .id(itemDto1.getId())
                .name(itemDto1.getName())
                .description(itemDto1.getDescription())
                .available(itemDto1.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(new ArrayList<>())
                .build();

        ItemDto itemDto2 = createItem(2, 1L);
        ItemDetailedDto itemDetailedDto2 = ItemDetailedDto.builder()
                .id(itemDto2.getId())
                .name(itemDto2.getName())
                .description(itemDto2.getDescription())
                .available(itemDto2.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(new ArrayList<>())
                .build();

        createItem(3, 2L);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(Arrays.asList(itemDetailedDto1, itemDetailedDto2))));
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
                        .param("text", "cool")
                        .param("from", "0")
                        .param("size", "20"))
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
        user.setId((long) number);
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
