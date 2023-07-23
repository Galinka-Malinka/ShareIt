package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
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
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        try {
            return UserMapper.toUserDto(userStorage.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException(String.format(
                    "Пользователь с %s уже зарегистрирован", userDto.getEmail()
            ));
        }
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User foundUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id " + userId));

        User user = UserMapper.toUser(userDto);

        if (user.getName() != null) {
            foundUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            foundUser.setEmail(user.getEmail());
        }
        try {
            return UserMapper.toUserDto(userStorage.saveAndFlush(foundUser));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException(String.format(
                    "Пользователь с %s уже зарегистрирован", userDto.getEmail()
            ));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(Long userId) {
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
    public void deleteById(Long userId) {
        userStorage.deleteById(userId);
    }
}
