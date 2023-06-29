package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemStorage {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    Collection<ItemDto> getItemsUser(Long userId);

    Collection<ItemDto> getItemsOnRequest(String text);
}
