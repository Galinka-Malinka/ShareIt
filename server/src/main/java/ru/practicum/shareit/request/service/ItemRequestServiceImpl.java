package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestStorage itemRequestStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(user, itemRequestDto);
        return ItemRequestMapper.toItemRequestDto(itemRequestStorage.save(itemRequest));
    }

    @Override
    public List<ItemRequestWithAnswersDto> getRequestOwner(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<ItemRequest> itemRequestList = itemRequestStorage.findAllByRequesterIdOrderByCreatedDesc(userId);

        return createItemRequestWithAnswersDtoList(itemRequestList);
    }

    @Override
    public List<ItemRequestWithAnswersDto> get(Long userId, Integer from, Integer size) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (from < 0) {
            throw new IllegalArgumentException("from не может быть меньше 0");
        }

        if (size < 1) {
            throw new IllegalArgumentException("size не может быть меньше 1");
        }

        Pageable sortedByCreatedDesc =
                PageRequest.of(from, size, Sort.by("created").descending());

        List<ItemRequest> allItemRequest =
                itemRequestStorage.findAllByRequesterIdNot(userId, sortedByCreatedDesc);

        return createItemRequestWithAnswersDtoList(allItemRequest);
    }

    @Override
    public ItemRequestWithAnswersDto getById(Long userId, Long requestId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        ItemRequest itemRequest = itemRequestStorage.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<ItemDto> answers = ItemMapper.toItemDtoList(itemStorage.findAllByRequestId(itemRequest.getId()));
        return ItemRequestMapper.toItemRequestWithAnswersDto(itemRequest, answers);
    }

    public List<ItemRequestWithAnswersDto> createItemRequestWithAnswersDtoList(List<ItemRequest> itemRequestList) {
        List<ItemRequestWithAnswersDto> result = new ArrayList<>();

        for (ItemRequest itemRequest : itemRequestList) {
            List<ItemDto> answers = ItemMapper.toItemDtoList(itemStorage.findAllByRequestId(itemRequest.getId()));
            result.add(ItemRequestMapper.toItemRequestWithAnswersDto(itemRequest, answers));
        }
        return result;
    }
}
