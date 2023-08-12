package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithAnswersDto> getRequestOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getRequestOwner(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithAnswersDto> get(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return itemRequestService.get(userId, from, size);

    }

    @GetMapping("/{requestId}")
    public ItemRequestWithAnswersDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
    //POST /requests — добавить новый запрос вещи. Основная часть запроса — текст запроса, где пользователь описывает,
    // какая именно вещь ему нужна.
    //GET /requests — получить список своих запросов вместе с данными об ответах на них. Для каждого запроса должны
    // указываться описание, дата и время создания и список ответов в формате: id вещи, название, её описание
    // description, а также requestId запроса и признак доступности вещи available. Так в дальнейшем, используя
    // указанные id вещей, можно будет получить подробную информацию о каждой вещи. Запросы должны возвращаться
    // в отсортированном порядке от более новых к более старым.
    //GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями.
    // С помощью этого эндпоинта пользователи смогут просматривать существующие запросы, на которые они могли бы
    // ответить. Запросы сортируются по дате создания: от более новых к более старым. Результаты должны возвращаться
    // постранично. Для этого нужно передать два параметра: from — индекс первого элемента, начиная с 0,
    // и size — количество элементов для отображения.
    //GET /requests/{requestId} — получить данные об одном конкретном запросе вместе с данными об ответах на него
    // в том же формате, что и в эндпоинте GET /requests. Посмотреть данные об отдельном запросе может любой
    // пользователь.
}
