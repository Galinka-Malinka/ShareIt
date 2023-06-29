package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        return userStorage.addUser(userDto);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        return userStorage.updateUser(userId, userDto);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userStorage.getUserById(userId);
    }

    @Override
    public Collection<UserDto> getUsers() {
        return userStorage.getUsers();
    }

    @Override
    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}
