package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestWithAnswersDto {

    Long id;

    @NotNull(message = "Необходимо добавить описание предмета")
    @NotBlank(message = "Описание предмета не может состоять из пустой строки")
    String description;

    LocalDateTime created;

    List<ItemDto> items = new ArrayList<>();

}
