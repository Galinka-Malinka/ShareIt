package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import javax.validation.ValidationException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    private final ItemMapper mapper;
    private Long id = 0L;
    private final Map<Long, Map<Long, Item>> items = new HashMap<>();

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Для добавления предмета необходимо указать его название");
        } else if (itemDto.getDescription() == null) {
            throw new ValidationException("Для добавления предмета необходимо добавить его описание");
        } else if (itemDto.getAvailable() == null) {
            throw new ValidationException("Для добавления предмета необходимо указать его статус" +
                    " возможности бронирования");
        }

        Item newItem = Item.builder()
                .id(++id)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .ownerId(userId)
                .available(itemDto.getAvailable())
                .build();

        Map<Long, Item> mapUserItems = new HashMap<>();
        if (items.containsKey(userId)) {
            mapUserItems = items.get(userId);
            mapUserItems.put(newItem.getId(), newItem);
            items.replace(userId, mapUserItems);
        } else {
            mapUserItems.put(newItem.getId(), newItem);
            items.put(userId, mapUserItems);
        }
        log.debug("Добавлен предмет {}", newItem);
        return mapper.toItemDto(newItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (!items.containsKey(userId)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет предметов для шеринга");
        }
        Optional<Item> updatedItem = Optional.empty();

        for (Item item : items.get(userId).values()) {
            if (item.getId().equals(itemId)) {

                if (itemDto.getAvailable() == null) {
                    itemDto.setAvailable(item.isAvailable());
                }
                if (itemDto.getDescription() == null) {
                    itemDto.setDescription(item.getDescription());
                }
                if (itemDto.getName() == null) {
                    itemDto.setName(item.getName());
                }

                Item itemForUpdating = mapper.toItem(userId, itemDto, itemId);

                items.get(userId).replace(itemId, itemForUpdating);
                updatedItem = Optional.of(itemForUpdating);
            }
        }

        if (updatedItem.isEmpty()) {
            throw new NotFoundException("У пользователя с id " + userId +
                    "нет предмета для обновления с id " + itemId);
        }
        return mapper.toItemDto(updatedItem.get());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Optional<ItemDto> optionalItemDto = Optional.empty();
        for (Map<Long, Item> itemMap : items.values()) {
            if (itemMap.containsKey(itemId)) {
                ItemDto itemDto = mapper.toItemDto(itemMap.get(itemId));
                optionalItemDto = Optional.of(itemDto);
            }
        }
        if (optionalItemDto.isEmpty()) {
            throw new NotFoundException("Предмета с Id " + itemId + " не обнаружено");
        }
        return optionalItemDto.get();
    }

    @Override
    public Collection<ItemDto> getItemsUser(Long userId) {
        if (!items.containsKey(userId)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет предметов для шеринга");
        }
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : items.get(userId).values()) {
            ItemDto itemDto = mapper.toItemDto(item);
            itemDtoList.add(itemDto);
        }
        return itemDtoList;
    }

    @Override
    public Collection<ItemDto> getItemsOnRequest(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        List<ItemDto> itemDtoSet = new ArrayList<>();
        for (Long ownerId : items.keySet()) {
            List<Item> listItemsOwner = new ArrayList<>(items.get(ownerId).values());
            for (Item item : listItemsOwner) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())
                        && item.isAvailable()) {
                    itemDtoSet.add(mapper.toItemDto(item));
                } else if (item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.isAvailable()) {
                    itemDtoSet.add(mapper.toItemDto(item));
                }
            }
        }
        return itemDtoSet;
    }
}
