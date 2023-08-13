package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto)
            throws ValidationException {
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) throws ValidationException {
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteById(@PathVariable Long userId) {
        userService.deleteById(userId);
    }
}
