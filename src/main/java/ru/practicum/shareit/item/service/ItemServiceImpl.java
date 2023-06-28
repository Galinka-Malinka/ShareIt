package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;

    @Override
    public Optional<Item> addItem(Long userId, ItemDto itemDto) {
        return itemStorage.addItem(userId, itemDto);
    }

    @Override
    public Optional<Item> updateItem(Long userId, Long itemId, ItemDto itemDto) {
        return itemStorage.updateItem(userId, itemId, itemDto);
    }

    @Override
    public Optional<ItemDto> getItemById(Long itemId) {
        return itemStorage.getItemById(itemId);
    }

    @Override
    public Collection<ItemDto> getItemsUser(Long userId) {
        return itemStorage.getItemsUser(userId);
    }

    @Override
    public Collection<ItemDto> getItemsOnRequest(String text) {
        return itemStorage.getItemsOnRequest(text);
    }
}
