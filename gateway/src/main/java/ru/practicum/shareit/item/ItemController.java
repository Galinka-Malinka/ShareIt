package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating Item {}, userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Updating ItemId={}, userId={}, itemDto {}", itemId, userId, itemDto);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getByUserIdAndItemId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getByUserIdAndItemId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                               Integer from,
                                               @Positive @RequestParam(name = "size", defaultValue = "10")
                                               Integer size) {
        log.info("Get items with userId={}, from={}, size={}", userId, from, size);
        return itemClient.getItemsUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsOnRequest(@NotNull @RequestParam(value = "text") String text,
                                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                    Integer from,
                                                    @Positive @RequestParam(name = "size", defaultValue = "10")
                                                    Integer size) {
        log.info("Get items with text {}, from={}, size={}", text, from, size);
        return itemClient.getItemsOnRequest(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
