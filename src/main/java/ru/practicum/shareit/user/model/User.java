package ru.practicum.shareit.user.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Имя пользователя не заданно")
    @NotBlank(message = "Имя пользователя не может состоять из пустой строки")
    String name;

    @Column(name = "email", unique = true, nullable = false)
    @NotNull(message = "email пользователя не задан")
    @NotBlank(message = "email пользователя не может состоять из пустой строки")
    @Email(message = "Не верный формат email")

    String email;
}
