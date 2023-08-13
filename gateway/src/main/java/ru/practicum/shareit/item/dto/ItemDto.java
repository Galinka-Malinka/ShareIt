package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    @NotNull(message = "Название предмету не заданно")
    @NotBlank(message = "Название предмета не может состоять из пустой строки")
    String name;

    @NotNull(message = "Необходимо добавить описание предмета")
    @NotBlank(message = "Описание предмета не может состоять из пустой строки")
    String description;

    @NotNull(message = "Необходимо указать статус возможности бронирования предмета")
    Boolean available;

    Long requestId;
}
