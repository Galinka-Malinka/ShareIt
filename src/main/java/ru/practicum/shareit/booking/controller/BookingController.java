package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingForAnswerDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestBody BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingForAnswerDto respondToBookingRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long bookingId,
                                                       @RequestParam(value = "approved") Boolean response) {
        return bookingService.respondToBookingRequest(userId, bookingId, response);
    }

    @GetMapping("/{bookingId}")
    public BookingForAnswerDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingForAnswerDto> getBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(required = false, defaultValue = "ALL")
                                                       String state) {
        return bookingService.getBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingForAnswerDto> getBookingsForOwnersItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(required = false, defaultValue = "ALL")
                                                               String state) {
        return bookingService.getBookingsForOwnersItems(userId, state);
    }
}
