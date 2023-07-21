package ru.practicum.shareit.user.dto;
//
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Data;
//import lombok.experimental.FieldDefaults;
//
//import javax.validation.constraints.Email;
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotNull;
//
//@Data
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class UserDto {
//    Long id;
//
//    @NotNull(message = "Имя пользователя не заданно")
//    @NotBlank(message = "Имя пользователя не может состоять из пустой строки")
//    private String name;
//
//    @NotNull(message = "email пользователя не задан")
//    @NotBlank(message = "email пользователя не может состоять из пустой строки")
//    @Email(message = "Не верный формат email")
//    private String email;
//}

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    private Long id;

    @NotNull
    @NotEmpty
    private String name;

    @Email
    @NotNull
    @NotEmpty
    private String email;
}