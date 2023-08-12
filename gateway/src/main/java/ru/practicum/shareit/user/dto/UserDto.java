package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @NotNull(message = "Имя пользователя не заданно")
    @NotBlank(message = "Имя пользователя не может состоять из пустой строки")
    private String name;

    @NotNull(message = "email пользователя не задан")
    @NotBlank(message = "email пользователя не может состоять из пустой строки")
    @Email(message = "Не верный формат email")
    private String email;
}
