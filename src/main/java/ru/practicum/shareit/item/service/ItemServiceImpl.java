package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailedDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Transactional
    @Override

    public ItemDto create(Long userId, ItemDto itemDto) {

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Необходимо указать статус бронирования при добавлении предмета");
        }

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Необходимо указать название предмета");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Необходимо добавить описание предмета");
        }

        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = ItemMapper.toItem(user, itemDto);

        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (!item.getOwner().equals(user)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет предмета с id " + itemId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDetailedDto getByUserIdAndItemId(Long userId, Long itemId) {
        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id " + itemId + " не найден"));

        List<Comment> comments = new ArrayList<>();

        if (!commentStorage.findByItemId(itemId).isEmpty()) {
            comments.addAll(commentStorage.findByItemId(itemId));
        }

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (userId.equals(item.getOwner().getId())) {
            List<Booking> bookings = bookingStorage.findByItemIdOrderByStart(itemId);
            bookings = bookings.stream().filter(e -> !e.getStatus().equals(Status.REJECTED))
                    .collect(Collectors.toList());

            LocalDateTime timeNow = LocalDateTime.now();
            for (Booking booking : bookings) {
                if (booking.getStart().isAfter(timeNow)) {
                    nextBooking = booking;
                } else {
                    lastBooking = booking;
                    break;
                }
            }
        }
        return ItemMapper.toItemDetailedDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public Collection<ItemDetailedDto> getItemsUser(Long userId) {
        List<ItemDetailedDto> itemDetailedDtoList = new ArrayList<>();
        List<Item> items = itemStorage.findByOwnerIdOrderById(userId);

        Booking lastBooking = null;
        Booking nextBooking = null;

        for (Item item : items) {

            List<Booking> bookings = bookingStorage.findByItemIdOrderByStart(item.getId());
            bookings = bookings.stream().filter(e -> !e.getStatus().equals(Status.REJECTED))
                    .collect(Collectors.toList());

            LocalDateTime timeNow = LocalDateTime.now();

            for (Booking booking : bookings) {
                if (booking.getStart().isAfter(timeNow)) {
                    nextBooking = booking;
                } else {
                    lastBooking = booking;
                    break;
                }
            }
            List<Comment> comments = commentStorage.findByItemId(item.getId());
            itemDetailedDtoList.add(ItemMapper.toItemDetailedDto(item, lastBooking, nextBooking, comments));

            lastBooking = null;
            nextBooking = null;
        }
        return itemDetailedDtoList;
    }

    @Override
    public Collection<ItemDto> getItemsOnRequest(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.toItemDtoList(itemStorage.getItemOnRequest(text));
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Необходимо ввести текст комментария");
        }

        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (bookingStorage.findByBookerIdAndItemId(userId, itemId).isEmpty()) {
            throw new ValidationException("Пользователь с id " + userId + " не может оставить комментарий," +
                    " т.к. он не арендовал предмет с id " + itemId);
        }

        LocalDateTime timeNow = LocalDateTime.now();
        List<Booking> bookings = bookingStorage.findByBookerIdAndItemId(userId, itemId).stream()
                .filter(b -> b.getEnd().isBefore(timeNow)).collect(Collectors.toList());

        if (bookings.isEmpty()) {
            throw new ValidationException("Пользователь не может оставить комментарий," +
                    " т.к. срок его аренды предмета ещё не закончился");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .created(timeNow)
                .build();

        return ItemMapper.toCommentDto(commentStorage.save(comment));
    }
}
