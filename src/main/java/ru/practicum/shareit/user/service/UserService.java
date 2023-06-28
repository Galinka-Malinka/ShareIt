package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserService {
    Optional<User> addUser(UserDto userDto);

    Optional<User> updateUser(Long userId, UserDto userDto);

    Optional<User> getUserById(Long userId);

    Collection<User> getUsers();

    void deleteUserById(Long userId);
}
