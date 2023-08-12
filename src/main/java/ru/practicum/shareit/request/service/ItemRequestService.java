package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestWithAnswersDto> getRequestOwner(Long userId);

    List<ItemRequestWithAnswersDto> get(Long userId, Integer from, Integer size);

    ItemRequestWithAnswersDto getById(Long userId, Long requestId);
}
