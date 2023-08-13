package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Transactional
    @Override
    public BookingForAnswerDto create(Long userId, BookingDto bookingDto) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemStorage.findById(bookingDto.getItemId()).orElseThrow(() ->
                new NotFoundException("Предмет с id " + bookingDto.getItemId() + " не найден"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Данный предмет не доступен для бронирования," +
                    " т.к. пользователь не может забронировать собственный предмет");
        }

        if (!item.isAvailable()) {
            throw new ValidationException("Предмет с id " + item.getId() + " в данный момент не доступен для аренды");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, user, Status.WAITING);

        return BookingMapper.toBookingForAnswerDto(bookingStorage.save(booking));
    }

    @Transactional
    @Override
    public BookingForAnswerDto respondToBookingRequest(Long userId, Long bookingId, Boolean response) {
        checkUser(userId);

        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + "не является владельцем вещи с id "
                    + booking.getItem().getId());
        }

        if (response) {
            if (booking.getStatus().equals(Status.APPROVED)) {
                throw new ValidationException("У предмета с id " + booking.getItem().getId()
                        + " уже установлен статус возможности бронирования");
            }

            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingForAnswerDto(bookingStorage.save(booking));
    }

    @Override
    public BookingForAnswerDto getByUserIdAndBookingId(Long userId, Long bookingId) {
        checkUser(userId);

        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + "не имеет доступ к бронированию с id "
                    + bookingId);
        }
        return BookingMapper.toBookingForAnswerDto(booking);
    }

    @Override
    public List<BookingForAnswerDto> getBookingsByUser(Long userId, String state, Integer from, Integer size) {
        checkUser(userId);

        List<Booking> bookings = new ArrayList<>();

        int page = from / size;

        Pageable sortedAndPageable =
                PageRequest.of(page, size, Sort.by("start").descending());
        Pageable sortedAndPageableByCurrent =
                PageRequest.of(page, size, Sort.by("start"));

        LocalDateTime timeNow = LocalDateTime.now();

        switch (state) {
            case "ALL":
                bookings.addAll(bookingStorage.findAllByBookerId(userId, sortedAndPageable));
                break;
            case "CURRENT":
                bookings.addAll(bookingStorage.findCurrentBookingsByBookerId(userId, timeNow,
                        sortedAndPageableByCurrent));
                break;
            case "PAST":
                bookings.addAll(bookingStorage.findByBookerIdAndEndInPast(userId, timeNow, sortedAndPageable));
                break;
            case "FUTURE":
                bookings.addAll(bookingStorage.findByBookerIdAndStartInFuture(userId, timeNow, sortedAndPageable));
                break;
            case "WAITING":
                bookings.addAll(bookingStorage
                        .findByBookerIdAndStatusContaining(userId, Status.WAITING, sortedAndPageable));
                break;
            case "REJECTED":
                bookings.addAll(bookingStorage
                        .findByBookerIdAndStatusContaining(userId, Status.REJECTED, sortedAndPageable));
                break;
        }
        return BookingMapper.toBookingForAnswerDtoList(bookings);
    }

    @Override
    public List<BookingForAnswerDto> getBookingsForOwnersItems(Long userId, String state, Integer from, Integer size) {
        checkUser(userId);
        if (itemStorage.findByOwnerId(userId).isEmpty()) {
            throw new NotFoundException("У пользователя с id " + userId + " нет предметов для шеринга");
        }
        List<Booking> bookings = new ArrayList<>();

        int page = from / size;

        Pageable sortedAndPageable =
                PageRequest.of(page, size, Sort.by("start").descending());

        LocalDateTime timeNow = LocalDateTime.now();

        switch (state) {
            case "ALL":
                bookings.addAll(bookingStorage.getAllBookingsForOwnersItems(userId, sortedAndPageable));
                break;
            case "CURRENT":
                bookings.addAll(bookingStorage
                        .getCurrentBookingsForOwnersItems(userId, timeNow, sortedAndPageable));
                break;
            case "PAST":
                bookings.addAll(bookingStorage.getPastBookingsForOwnersItems(userId, timeNow, sortedAndPageable));
                break;
            case "FUTURE":
                bookings.addAll(bookingStorage.getFutureBookingsForOwnersItems(userId, timeNow, sortedAndPageable));
                break;
            case "WAITING":
                bookings.addAll(bookingStorage
                        .getBookingsForOwnersWithStatusContaining(userId, Status.WAITING, sortedAndPageable));
                break;
            case "REJECTED":
                bookings.addAll(bookingStorage
                        .getBookingsForOwnersWithStatusContaining(userId, Status.REJECTED, sortedAndPageable));
                break;
        }
        return BookingMapper.toBookingForAnswerDtoList(bookings);
    }

    public void checkUser(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}
