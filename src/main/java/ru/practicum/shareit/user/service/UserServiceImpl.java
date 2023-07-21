package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userStorage.saveAndFlush(UserMapper.toUser(userDto)));
        } catch (ConstraintViolationException e) {
            throw new RuntimeException("Пользователь с email " + userDto.getEmail() + " уже зарегистрирован");
        }
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        if ((userDto.getEmail() != null && !userDto.getEmail().isBlank())) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> getUsers() {
        return UserMapper.toUserDtoList(userStorage.findAll());
    }

    @Override
    public void deleteUserById(Long userId) {
        userStorage.deleteById(userId);
    }
}
