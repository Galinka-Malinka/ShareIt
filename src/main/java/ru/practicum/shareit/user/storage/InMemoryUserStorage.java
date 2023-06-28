package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private Long id = 0L;
    private final Map<Long, User> users = new HashMap<>();
    private final UserMapper mapper;

    @Override
    public Optional<User> addUser(UserDto userDto) {
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(userDto.getEmail())) {
                throw new RuntimeException("Данный email уже используется");
            }
        }

        if (userDto.getName() == null || userDto.getEmail() == null) {
            throw new ValidationException("Не указаны все необходимые данные для пользователя");
        }

        User newUser = mapper.convertUserDtoToUser(++id, userDto);

        this.users.put(newUser.getId(), newUser);
        log.debug("Добавление пользователя: {}", newUser);
        return Optional.of(newUser);
    }

    @Override
    public Optional<User> updateUser(Long userId, UserDto userDto) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
        User user = users.get(userId);

        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(userDto.getEmail()) && !user.getEmail().equals(userDto.getEmail())) {
                throw new RuntimeException("Данный email уже используется");
            }
        }

        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        } else if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }

        User newUser = mapper.convertUserDtoToUser(userId, userDto);

        this.users.replace(userId, newUser);
        log.debug("Обновление пользователя: {}", newUser);
        return Optional.of(newUser);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
        return Optional.of(users.get(userId));
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
        users.remove(userId);
        log.debug("Пользователь с id {} удалён", userId);
    }
}
