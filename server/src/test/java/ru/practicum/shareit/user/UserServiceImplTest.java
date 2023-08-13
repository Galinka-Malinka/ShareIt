package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    void shouldCreateUser() {

        UserDto userDto = UserDto.builder()
                .name("User")
                .email("User@mail.ru")
                .build();

        userService.create(userDto);

        TypedQuery<User> queryForUser = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = queryForUser.setParameter("email", userDto.getEmail()).getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));

        assertThrows(AlreadyExistsException.class, () -> userService.create(userDto),
                "Пользователь с " + userDto.getEmail() + " уже зарегистрирован");
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser(1L);

        UserDto userDtoForUpdate = UserDto.builder()
                .name("UpdatedUser")
                .email("UpdatedUser@mail.ru")
                .build();

        userService.update(user.getId(), userDtoForUpdate);

        TypedQuery<User> queryForUpdatedUser = em.createQuery("Select u from User u where u.id = :id", User.class);
        User updatedUser = queryForUpdatedUser.setParameter("id", user.getId()).getSingleResult();

        assertThat(updatedUser.getId(), notNullValue());
        assertThat(updatedUser.getName(), equalTo(userDtoForUpdate.getName()));
        assertThat(updatedUser.getEmail(), equalTo(userDtoForUpdate.getEmail()));

        User user2 = createUser(2L);
        assertThrows(AlreadyExistsException.class, () -> userService.update(user.getId(), UserMapper.toUserDto(user2)),
                "Пользователь с " + user2.getEmail() + " уже зарегистрирован");
    }

    @Test
    void shouldGetById() {
        User user = createUser(1L);

        UserDto excludedUserDto = userService.getById(user.getId());

        assertThat(excludedUserDto.getId(), is(user.getId()));
        assertThat(excludedUserDto.getName(), equalTo(user.getName()));
        assertThat(excludedUserDto.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void shouldGetUsers() {
        User user1 = createUser(1L);
        User user2 = createUser(2L);

        List<UserDto> checkList = new ArrayList<>();
        checkList.add(UserMapper.toUserDto(user1));
        checkList.add(UserMapper.toUserDto(user2));

        List<UserDto> users = userService.getUsers();

        assertThat(users, notNullValue());
        assertThat(users, equalTo(checkList));
    }

    @Test
    void shouldDeleteUser() {
        User user = createUser(1L);

        userService.deleteById(user.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(user.getId()),
                "Пользователь с id " + user.getId() + " не найден");
    }

    public User createUser(Long id) {
        UserDto userDto = UserDto.builder()
                .id(id)
                .name("User" + id)
                .email("User" + id + "@mail.ru")
                .build();

        userService.create(userDto);

        TypedQuery<User> queryForUser = em.createQuery("Select u from User u where u.email = :email", User.class);
        return queryForUser.setParameter("email", userDto.getEmail()).getSingleResult();
    }
}
