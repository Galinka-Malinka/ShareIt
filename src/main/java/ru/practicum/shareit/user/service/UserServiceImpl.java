package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public Optional<User> addUser(UserDto userDto) {
        return userStorage.addUser(userDto);
    }

    @Override
    public Optional<User> updateUser(Long userId, UserDto userDto) {
        return userStorage.updateUser(userId, userDto);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userStorage.getUserById(userId);
    }

    @Override
    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    @Override
    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}
