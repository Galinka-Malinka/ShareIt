package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;

    @NotNull(message = "Необходимо добавить содержимое комментария")
    @NotBlank(message = "Комментарий не может состоять из пустой строки")
    String text;

    Long itemId;

    String authorName;

    LocalDateTime created;
}
