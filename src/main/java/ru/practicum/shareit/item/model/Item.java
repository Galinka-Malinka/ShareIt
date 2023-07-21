package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.util.Set;

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
    String name;

    @Column(name = "description", nullable = false)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    User owner;

    @Column(name = "is_available", nullable = false)
    boolean available;

    @OneToMany
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    private Set<Booking> bookings;

    @OneToMany
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    private Set<Comment> comments;
}
