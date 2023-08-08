package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker, Status status) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(status)
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static BookingForAnswerDto toBookingForAnswerDto(Booking booking) {
        return BookingForAnswerDto.builder()
                .id(booking.getId())
                .itemDto(ItemMapper.toItemDto(booking.getItem()))
                .bookerDto(UserMapper.toUserDto(booking.getBooker()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static List<BookingForAnswerDto> toBookingForAnswerDtoList(List<Booking> bookings) {
        List<BookingForAnswerDto> bookingForAnswerDtoList = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingForAnswerDtoList.add(toBookingForAnswerDto(booking));
        }
        return bookingForAnswerDtoList;
    }
}
