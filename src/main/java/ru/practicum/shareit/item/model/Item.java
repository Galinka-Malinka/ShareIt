package ru.practicum.shareit.item.model;

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
public class Item {
    final Long id;
    @NotNull(message = "Название предмету не заданно")
    @NotBlank(message = "Название предмета не может состоять из пустой строки")
    String name;
    @NotNull(message = "Необходимо добавить описание предмета")
    String description;
    @NotNull(message = "Необходимо указать id владельца")
    final Long ownerId;
    @NotNull(message = "Необходимо указать статус возможности бронирования предмета")
    boolean available;
}
