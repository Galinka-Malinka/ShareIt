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
    public BookingForAnswerDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingForAnswerDto respondToBookingRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long bookingId,
                                                       @RequestParam(value = "approved") Boolean response) {
        return bookingService.respondToBookingRequest(userId, bookingId, response);
    }

    @GetMapping("/{bookingId}")
    public BookingForAnswerDto getByUserIdAndBookingId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long bookingId) {
        return bookingService.getByUserIdAndBookingId(userId, bookingId);
    }

    @GetMapping
    public List<BookingForAnswerDto> getBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(required = false, defaultValue = "ALL")
                                                       String state,
                                                       @RequestParam(required = false, defaultValue = "0")
                                                       Integer from,
                                                       @RequestParam(required = false, defaultValue = "10")
                                                       Integer size) {
        return bookingService.getBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingForAnswerDto> getBookingsForOwnersItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(required = false, defaultValue = "ALL")
                                                               String state,
                                                               @RequestParam(required = false, defaultValue = "0")
                                                               Integer from,
                                                               @RequestParam(required = false, defaultValue = "10")
                                                               Integer size) {
        return bookingService.getBookingsForOwnersItems(userId, state, from, size);
    }
}
