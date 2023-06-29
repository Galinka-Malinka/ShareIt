package ru.practicum.shareit.user.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    final Long id;
    @NotNull(message = "Имя пользователя не заданно")
    @NotBlank(message = "Имя пользователя не может состоять из пустой строки")
    String name;
    @NotNull(message = "email пользователя не задан")
    @NotBlank(message = "email пользователя не может состоять из пустой строки")
    @Email(message = "Не верный формат email")
    final String email;
}
