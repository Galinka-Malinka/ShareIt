package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void shouldCreateItemRequest() {
        User user = createUser(1);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Description for the ItemRequest")
                .created(LocalDateTime.now())
                .build();

        itemRequestService.create(user.getId(), itemRequestDto);

        TypedQuery<ItemRequest> queryForItemRequest = em.createQuery("Select i from ItemRequest i where i.id = :id",
                ItemRequest.class);
        ItemRequest itemRequest = queryForItemRequest.setParameter("id", itemRequestDto.getId()).getSingleResult();

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));

        assertThrows(NotFoundException.class, () -> itemRequestService.create(2L, itemRequestDto),
                "Пользователь с id 2 не найден");

        assertThrows(NullPointerException.class, () -> ItemRequestMapper.toItemRequest(null, null));
    }

    @Test
    void shouldGetRequestOwner() {
        User user1 = createUser(1);

        ItemRequest itemRequest1 = createItemRequest(1L, user1.getId());
        Item item1 = createItem(1L, user1.getId(), itemRequest1.getId());

        List<ItemDto> answersForRequest1 = new ArrayList<>();
        answersForRequest1.add(ItemMapper.toItemDto(item1));

        ItemRequestWithAnswersDto itemRequestWithAnswersDto1 = ItemRequestMapper
                .toItemRequestWithAnswersDto(itemRequest1, answersForRequest1);

        ItemRequest itemRequest2 = createItemRequest(2L, user1.getId());
        Item item2 = createItem(2L, user1.getId(), itemRequest2.getId());
        Item item3 = createItem(3L, user1.getId(), itemRequest2.getId());

        List<ItemDto> answersForRequest2 = new ArrayList<>();
        answersForRequest2.add(ItemMapper.toItemDto(item2));
        answersForRequest2.add(ItemMapper.toItemDto(item3));

        ItemRequestWithAnswersDto itemRequestWithAnswersDto2 = ItemRequestMapper
                .toItemRequestWithAnswersDto(itemRequest2, answersForRequest2);

        User user2 = createUser(2);

        createItemRequest(3L, user2.getId());

        List<ItemRequestWithAnswersDto> reqestList = itemRequestService.getRequestOwner(user1.getId());
        List<ItemRequestWithAnswersDto> checkList = new ArrayList<>();
        checkList.add(itemRequestWithAnswersDto2);
        checkList.add(itemRequestWithAnswersDto1);

        assertThat(reqestList, notNullValue());
        assertThat(reqestList, equalTo(checkList));

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestOwner(3L),
                "Пользователь с id 3 не найден");
    }

    @Test
    void shouldGetAllRequestWithPageable() {
        User user1 = createUser(1);

        ItemRequest itemRequest1 = createItemRequest(1L, user1.getId());
        createItem(1L, user1.getId(), itemRequest1.getId());

        ItemRequest itemRequest2 = createItemRequest(2L, user1.getId());
        Item item2 = createItem(2L, user1.getId(), itemRequest2.getId());
        Item item3 = createItem(3L, user1.getId(), itemRequest2.getId());

        List<ItemDto> itemsFromItemRequest2 = new ArrayList<>();
        itemsFromItemRequest2.add(ItemMapper.toItemDto(item2));
        itemsFromItemRequest2.add(ItemMapper.toItemDto(item3));

        ItemRequest itemRequest3 = createItemRequest(3L, user1.getId());
        ItemRequest itemRequest4 = createItemRequest(4L, user1.getId());

        User user2 = createUser(2);

        createItemRequest(5L, user2.getId());

        List<ItemRequestWithAnswersDto> itemRequestWithAnswersDtoList = itemRequestService
                .get(user2.getId(), 0, 3);
        List<ItemRequestWithAnswersDto> checkList = new ArrayList<>();
        checkList.add(ItemRequestMapper.toItemRequestWithAnswersDto(itemRequest4, new ArrayList<>()));
        checkList.add(ItemRequestMapper.toItemRequestWithAnswersDto(itemRequest3, new ArrayList<>()));
        checkList.add(ItemRequestMapper.toItemRequestWithAnswersDto(itemRequest2, itemsFromItemRequest2));

        assertThat(itemRequestWithAnswersDtoList, notNullValue());
        assertThat(itemRequestWithAnswersDtoList, equalTo(checkList));

        assertThrows(NotFoundException.class, () -> itemRequestService.get(3L, 0, 3),
                "Пользователь с id 3 не найден");
        assertThrows(IllegalArgumentException.class, () -> itemRequestService.get(user2.getId(), -1, 3),
                "from не может быть меньше 0");
        assertThrows(IllegalArgumentException.class, () -> itemRequestService.get(user2.getId(), 0, 0),
                "size не может быть меньше 1");
    }

    @Test
    void shouldGetRequestById() {
        User user = createUser(1);

        ItemRequest itemRequest = createItemRequest(1L, user.getId());
        Item item = createItem(1L, user.getId(), itemRequest.getId());

        List<ItemDto> answersForRequest = new ArrayList<>();
        answersForRequest.add(ItemMapper.toItemDto(item));

        ItemRequestWithAnswersDto checkItemRequestWithAnswersDto = ItemRequestMapper
                .toItemRequestWithAnswersDto(itemRequest, answersForRequest);

        ItemRequestWithAnswersDto itemRequestWithAnswersDto = itemRequestService
                .getById(user.getId(), itemRequest.getId());

        assertThat(itemRequestWithAnswersDto, notNullValue());
        assertThat(itemRequestWithAnswersDto, equalTo(checkItemRequestWithAnswersDto));

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(2L, itemRequest.getId()),
                "Пользователь с id 2 не найден");

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(user.getId(), 2L),
                "Запрос с id 2 не найден");
    }


    public User createUser(int id) {
        UserDto userDto = UserDto.builder()
                .name("User" + id)
                .email("User" + id + "@mail.ru")
                .build();

        userService.create(userDto);

        TypedQuery<User> queryForUser = em.createQuery("Select u from User u where u.email = :email", User.class);
        return queryForUser.setParameter("email", userDto.getEmail()).getSingleResult();
    }

    public Item createItem(Long itemId, Long userId, Long itemRequestId) {
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Name" + itemId)
                .description("Description for Item" + itemId)
                .requestId(itemRequestId)
                .available(true)
                .build();

        itemService.create(userId, itemDto);

        TypedQuery<Item> queryForItem = em.createQuery("Select i from Item i where i.id = :id",
                Item.class);
        return queryForItem.setParameter("id", itemDto.getId()).getSingleResult();
    }

    public ItemRequest createItemRequest(Long id, Long userId) {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(id)
                .description("Description for the ItemRequest" + id)
                .created(LocalDateTime.now())
                .build();

        itemRequestService.create(userId, itemRequestDto);

        TypedQuery<ItemRequest> queryForItemRequest = em.createQuery("Select i from ItemRequest i where i.id = :id",
                ItemRequest.class);
        return queryForItemRequest.setParameter("id", id).getSingleResult();
    }


}
