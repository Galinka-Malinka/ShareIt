package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingForAnswerDto {
    Long id;
    @JsonProperty("item")
    ItemDto itemDto;

    @JsonProperty("booker")
    UserDto bookerDto;

    Status status;

    LocalDateTime start;

    LocalDateTime end;
}
