package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;

import java.util.List;

public interface BookingService {
    BookingForAnswerDto addBooking(Long userId, BookingDto bookingDto);

    BookingForAnswerDto respondToBookingRequest(Long userId, Long bookingId, Boolean response);

    BookingForAnswerDto getBookingById(Long userId, Long bookingId);

    List<BookingForAnswerDto> getBookingsByUser(Long userId, String state);

    List<BookingForAnswerDto> getBookingsForOwnersItems(Long userId, String state);
}
