package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private final ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
            .id(1L)
            .description("Description for the request.")
            .created(LocalDateTime.now())
            .build();

    private final ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
            .id(2L)
            .description("Description for the request2.")
            .created(LocalDateTime.now())
            .build();

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Name")
            .description("Description")
            .requestId(1L)
            .available(true)
            .build();
    private final ItemRequestWithAnswersDto itemRequestWithAnswersDto1 = ItemRequestWithAnswersDto.builder()
            .id(1L)
            .description("Description for the request1.")
            .created(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();
    private final ItemRequestWithAnswersDto itemRequestWithAnswersDto2 = ItemRequestWithAnswersDto.builder()
            .id(2L)
            .description("Description for the request2.")
            .created(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();


    @Test
    void shouldCreateItemRequest() throws Exception {
        when(itemRequestService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(itemRequestDto1);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(itemRequestDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto1.getDescription()), String.class));
    }

    @Test
    void shouldGetRequestOwner() throws Exception {
        itemRequestWithAnswersDto1.getItems().add(itemDto);
        List<ItemRequestWithAnswersDto> list = new ArrayList<>();
        list.add(itemRequestWithAnswersDto2);
        list.add(itemRequestWithAnswersDto1);

        when(itemRequestService.getRequestOwner(anyLong())).thenReturn(list);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestWithAnswersDto2.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithAnswersDto2.getDescription()),
                        String.class))
                .andExpect(jsonPath("$[0].items",
                        is(itemRequestWithAnswersDto2.getItems())))
                .andExpect(jsonPath("$[1].id", is(itemRequestWithAnswersDto1.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequestWithAnswersDto1.getDescription()),
                        String.class))
                .andExpect(jsonPath("$[1].items", notNullValue()));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        List<ItemRequestWithAnswersDto> itemRequestDtoList = new ArrayList<>();
        itemRequestDtoList.add(itemRequestWithAnswersDto2);
        itemRequestDtoList.add(itemRequestWithAnswersDto1);

        when(itemRequestService.get(anyLong(), anyInt(), anyInt())).thenReturn(itemRequestDtoList);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestWithAnswersDto2.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithAnswersDto2.getDescription()), String.class))
                .andExpect(jsonPath("$[1].id", is(itemRequestWithAnswersDto1.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequestWithAnswersDto1.getDescription()), String.class));
    }

    @Test
    void shouldGetRequestById() throws Exception {

        when(itemRequestService.getById(anyLong(), anyLong())).thenReturn(itemRequestWithAnswersDto1);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestWithAnswersDto1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestWithAnswersDto1.getDescription()),
                        String.class));
    }
}
