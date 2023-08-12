package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailedDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDetailedDto getByUserIdAndItemId(Long userId, Long itemId);

    List<ItemDetailedDto> getItemsUser(Long userId, Integer from, Integer size);

    List<ItemDto> getItemsOnRequest(String text, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
