package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;

    @NotNull(message = "Название предмету не заданно")
    @NotBlank(message = "Название предмета не может состоять из пустой строки")
    String name;

    @NotNull(message = "Необходимо добавить описание предмета")
    @NotBlank(message = "Описание предмета не может состоять из пустой строки")
    String description;

    @NotNull(message = "Необходимо указать статус возможности бронирования предмета")
    Boolean available;
}
