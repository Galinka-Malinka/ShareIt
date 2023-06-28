package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Optional<Item> addItem(Long userId, ItemDto itemDto);

    Optional<Item> updateItem(Long userId, Long itemId, ItemDto itemDto);

    Optional<ItemDto> getItemById(Long itemId);

    Collection<ItemDto> getItemsUser(Long userId);

    Collection<ItemDto> getItemsOnRequest(String text);
}
