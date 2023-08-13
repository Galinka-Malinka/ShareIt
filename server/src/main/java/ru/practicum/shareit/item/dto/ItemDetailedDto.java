package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDetailedDto {
    Long id;

    String name;

    String description;

    Boolean available;

    BookingDto lastBooking;

    BookingDto nextBooking;

    List<CommentDto> comments;
}
