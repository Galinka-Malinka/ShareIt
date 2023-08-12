package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .name("User1")
            .email("User1@mail.ru")
            .build();

    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("Name")
            .description("Description")
            .requestId(1L)
            .available(true)
            .build();
    private final ItemDto itemDto2 = ItemDto.builder()
            .id(1L)
            .name("Name")
            .description("Description")
            .requestId(1L)
            .available(true)
            .build();

    private final BookingDto bookingDto1 = BookingDto.builder()
            .id(1L)
            .itemId(1L)
            .bookerId(1L)
            .status(Status.WAITING)
            .start(LocalDateTime.now())
            .end(LocalDateTime.now().plusDays(3))
            .build();

    private final BookingDto bookingDto2 = BookingDto.builder()
            .id(1L)
            .itemId(1L)
            .bookerId(1L)
            .status(Status.WAITING)
            .start(LocalDateTime.now().plusDays(5))
            .end(LocalDateTime.now().plusDays(7))
            .build();

    private final BookingForAnswerDto bookingForAnswerDto1 = BookingForAnswerDto.builder()
            .id(1L)
            .itemDto(itemDto1)
            .bookerDto(userDto1)
            .status(Status.WAITING)
            .start(bookingDto1.getStart())
            .end(bookingDto1.getEnd())
            .build();

    private final BookingForAnswerDto bookingForAnswerDto2 = BookingForAnswerDto.builder()
            .id(1L)
            .itemDto(itemDto2)
            .bookerDto(userDto1)
            .status(Status.WAITING)
            .start(bookingDto2.getStart())
            .end(bookingDto2.getEnd())
            .build();

    @Test
    public void shouldCreateBooking() throws Exception {

        when(bookingService.create(anyLong(), any(BookingDto.class))).thenReturn(bookingForAnswerDto1);
        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(bookingDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingForAnswerDto1.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingForAnswerDto1.getBookerDto().getId()),
                        Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingForAnswerDto1.getItemDto().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingForAnswerDto1.getStatus().toString())));
    }

    @Test
    void shouldRespondToBookingRequest() throws Exception {
        bookingForAnswerDto1.setStatus(Status.APPROVED);
        when(bookingService.respondToBookingRequest(anyLong(), anyLong(), any(Boolean.class)))
                .thenReturn(bookingForAnswerDto1);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingForAnswerDto1.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingForAnswerDto1.getBookerDto().getId()),
                        Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingForAnswerDto1.getItemDto().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())));
    }

    @Test
    void shouldGetByUserIdAndBookingId() throws Exception {
        when(bookingService.getByUserIdAndBookingId(anyLong(), anyLong())).thenReturn(bookingForAnswerDto1);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingForAnswerDto1.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingForAnswerDto1.getBookerDto().getId()),
                        Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingForAnswerDto1.getItemDto().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingForAnswerDto1.getStatus().toString())));

    }

    @Test
    void shouldGetBookingsByUser() throws Exception {
        List<BookingForAnswerDto> bookingForAnswerDtoList = new ArrayList<>();
        bookingForAnswerDtoList.add(bookingForAnswerDto2);
        bookingForAnswerDtoList.add(bookingForAnswerDto1);

        when(bookingService.getBookingsByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookingForAnswerDtoList);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingForAnswerDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookingForAnswerDto1.getId()), Long.class));

    }

    @Test
    void shouldGetBookingsForOwnersItems() throws Exception {
        List<BookingForAnswerDto> bookingForAnswerDtoList = new ArrayList<>();
        bookingForAnswerDtoList.add(bookingForAnswerDto2);
        bookingForAnswerDtoList.add(bookingForAnswerDto1);

        when(bookingService.getBookingsForOwnersItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookingForAnswerDtoList);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingForAnswerDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookingForAnswerDto1.getId()), Long.class));
    }
}
