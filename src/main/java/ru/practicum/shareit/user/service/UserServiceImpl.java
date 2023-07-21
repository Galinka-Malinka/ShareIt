package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getEmail() == null) {
            throw new ValidationException("Не указаны все необходимые данные для пользователя");
        }

        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        return UserMapper.toUserDto(userStorage.save(user));
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

    @Override
    public UserDto getUserById(Long userId) {
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getUsers() {
        return UserMapper.toUserDtoList(userStorage.findAll());
    }

    @Override
    public void deleteUserById(Long userId) {
        userStorage.deleteById(userId);
    }
}
