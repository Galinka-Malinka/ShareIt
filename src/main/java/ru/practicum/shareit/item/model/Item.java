package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */

@Entity
@Table(name = "items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "Название предмету не заданно")
    @NotBlank(message = "Название предмета не может состоять из пустой строки")
    String name;

    @Column(name = "description", nullable = false)
    @NotNull(message = "Необходимо добавить описание предмета")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    User owner;

    @Column(name = "is_available", nullable = false)
    @NotNull(message = "Необходимо указать статус возможности бронирования предмета")
    boolean available;
}
