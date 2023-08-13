package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailedDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
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
public class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    void shouldCreateItem() {
        User user = createUser(1);

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description for Item")
                .available(true)
                .build();

        itemService.create(user.getId(), itemDto);

        TypedQuery<Item> queryForItem = em.createQuery("Select i from Item i where i.id = :id",
                Item.class);
        Item item = queryForItem.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.isAvailable(), equalTo(itemDto.getAvailable()));

        assertThrows(NotFoundException.class, () -> itemService.create(2L, itemDto),
                "Пользователь с id 2 не найден");
    }

    @Test
    void shouldCreateItemOnRequest() {
        User user = createUser(1);
        ItemRequest itemRequest = createItemRequest(1L, user.getId());
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description for Item")
                .available(true)
                .requestId(itemRequest.getId())
                .build();

        itemService.create(user.getId(), itemDto);

        TypedQuery<Item> queryForItem = em.createQuery("Select i from Item i where i.id = :id",
                Item.class);
        Item item = queryForItem.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.isAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getRequest().getId(), equalTo(itemDto.getRequestId()));

        itemDto.setRequestId(2L);
        assertThrows(NotFoundException.class, () -> itemService.create(2L, itemDto));
    }

    @Test
    void shouldUpdateItem() {
        User user = createUser(1);
        Item item = createItem(1L, user.getId(), null);
        itemService.create(user.getId(), ItemMapper.toItemDto(item));

        ItemDto itemDto = ItemDto.builder()
                .name("UpdatedItem")
                .description("Description for UpdatedItem")
                .available(false)
                .build();

        itemService.update(user.getId(), item.getId(), itemDto);

        TypedQuery<Item> queryForUpdatedItem = em.createQuery("Select i from Item i where i.id = :id",
                Item.class);
        Item updatedItem = queryForUpdatedItem.setParameter("id", item.getId()).getSingleResult();

        assertThat(updatedItem.getId(), equalTo(item.getId()));
        assertThat(updatedItem.getName(), equalTo(itemDto.getName()));
        assertThat(updatedItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(updatedItem.isAvailable(), equalTo(itemDto.getAvailable()));

        assertThrows(NotFoundException.class, () -> itemService.update(2L, item.getId(), itemDto),
                "Пользователь с id 2 не найден");

        assertThrows(NotFoundException.class, () -> itemService.update(user.getId(), 5L, itemDto),
                "Предмет с id 5 не найден");

        User user2 = createUser(2);
        assertThrows(NotFoundException.class, () -> itemService.update(user2.getId(), item.getId(), itemDto),
                "У пользователя с id " + user2.getId() + " нет предмета с id " + item.getId());
    }

    @Test
    void shouldGetByUserIdAndItemId() throws InterruptedException {
        User user = createUser(1);
        Item item = createItem(1L, user.getId(), null);

        ItemDetailedDto checkItemDetailedDto1 = ItemDetailedDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(new ArrayList<>())
                .build();

        ItemDetailedDto itemDetailedDto = itemService.getByUserIdAndItemId(user.getId(), item.getId());

        assertThat(itemDetailedDto, equalTo(checkItemDetailedDto1));

        assertThrows(NotFoundException.class, () -> itemService.getByUserIdAndItemId(2L, item.getId()),
                "Пользователь с id 2 не найден");

        assertThrows(NotFoundException.class, () -> itemService.getByUserIdAndItemId(user.getId(), 2L),
                "Предмет с id 2 не найден");

        User user2 = createUser(2);

        BookingDto bookingDto1 = BookingDto.builder()
                .id(1L)
                .itemId(item.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(user2.getId(), bookingDto1);

        BookingDto bookingDto2 = BookingDto.builder()
                .id(2L)
                .itemId(item.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.create(user2.getId(), bookingDto2);

        ItemDetailedDto checkItemDetailedDto2 = ItemDetailedDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .lastBooking(bookingDto1)
                .nextBooking(bookingDto2)
                .comments(new ArrayList<>())
                .build();

        Thread.sleep(3_000);

        ItemDetailedDto itemDetailedDto2 = itemService.getByUserIdAndItemId(user.getId(), item.getId());

        assertThat(itemDetailedDto2, equalTo(checkItemDetailedDto2));
    }

    @Test
    void shouldGetItemsUser() throws InterruptedException {
        User user = createUser(1);
        Item item1 = createItem(1L, user.getId(), null);
        Item item2 = createItem(2L, user.getId(), null);
        Item item3 = createItem(3L, user.getId(), null);
        Item item4 = createItem(4L, user.getId(), null);
        Item item5 = createItem(5L, user.getId(), null);

        User user2 = createUser(2);

        BookingDto bookingDto1 = BookingDto.builder()
                .id(1L)
                .itemId(item1.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(user2.getId(), bookingDto1);

        BookingDto bookingDto2 = BookingDto.builder()
                .id(2L)
                .itemId(item1.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.create(user2.getId(), bookingDto2);

        BookingDto bookingDto3 = BookingDto.builder()
                .id(3L)
                .itemId(item2.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(user2.getId(), bookingDto3);
        bookingDto3.setId(3L);

        BookingDto bookingDto4 = BookingDto.builder()
                .id(4L)
                .itemId(item2.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.create(user2.getId(), bookingDto4);
        bookingDto4.setId(4L);

        Thread.sleep(3_000);

        ItemDetailedDto itemDetailedDto1 = itemService.getByUserIdAndItemId(user.getId(), item1.getId());

        List<ItemDetailedDto> checkList1 = new ArrayList<>();
        checkList1.add(itemDetailedDto1);
        checkList1.add(ItemMapper.toItemDetailedDto(item2,
                BookingMapper.toBooking(bookingDto3, item2, user2, Status.WAITING),
                BookingMapper.toBooking(bookingDto4, item2, user2, Status.WAITING), new ArrayList<>()));
        checkList1.add(ItemMapper.toItemDetailedDto(item3, null, null, new ArrayList<>()));
        checkList1.add(ItemMapper.toItemDetailedDto(item4, null, null, new ArrayList<>()));
        checkList1.add(ItemMapper.toItemDetailedDto(item5, null, null, new ArrayList<>()));

        List<ItemDetailedDto> itemDetailedDtoList1 = itemService.getItemsUser(user.getId(), 0, 10);

        assertThat(itemDetailedDtoList1, notNullValue());
        assertThat(itemDetailedDtoList1, equalTo(checkList1));

        List<ItemDetailedDto> checkList2 = new ArrayList<>();
        checkList2.add(ItemMapper.toItemDetailedDto(item5, null, null, new ArrayList<>()));

        List<ItemDetailedDto> itemDetailedDtoList2 = itemService.getItemsUser(user.getId(), 4, 2);

        assertThat(itemDetailedDtoList2, notNullValue());
        assertThat(itemDetailedDtoList2, equalTo(checkList2));

        assertThrows(NotFoundException.class, () -> itemService.getItemsUser(3L, 0, 10),
                "Пользователь с id 3 не найден");
//        assertThrows(IllegalArgumentException.class, () -> itemService.getItemsUser(user.getId(), -1, 10),
//                "from не может быть меньше 0");
//        assertThrows(IllegalArgumentException.class, () -> itemService.getItemsUser(user.getId(), 0, 0),
//                "size не может быть меньше 1");
    }

    @Test
    void shouldGetItemOnRequest() {
        User user1 = createUser(1);
        User user2 = createUser(2);

        Item item1 = createItem(1L, user1.getId(), null);
        item1.setName(item1.getName() + " cool");

        Item item2 = createItem(2L, user2.getId(), null);
        item2.setDescription(item2.getDescription() + " cool");

        Item item3 = createItem(3L, user1.getId(), null);
        item3.setName("Cool " + item3.getName());
        item3.setDescription("Cool " + item3.getDescription());

        createItem(4L, user2.getId(), null);

        List<ItemDto> checkList = new ArrayList<>();
        checkList.add(ItemMapper.toItemDto(item1));
        checkList.add(ItemMapper.toItemDto(item2));
        checkList.add(ItemMapper.toItemDto(item3));

        List<ItemDto> itemDtoList = itemService.getItemsOnRequest("cool", 0, 10);

        assertThat(itemDtoList, notNullValue());
        assertThat(itemDtoList, equalTo(checkList));

//        assertThrows(IllegalArgumentException.class, () -> itemService.getItemsOnRequest("cool", -1, 10),
//                "from не может быть меньше 0");
//        assertThrows(IllegalArgumentException.class, () -> itemService.getItemsOnRequest("cool", 0, 0),
//                "size не может быть меньше 1");

        List<ItemDto> itemDtoListEmpty = itemService.getItemsOnRequest("", 0, 10);
        assertThat(itemDtoListEmpty.size(), is(0));

    }

    @Test
    void shouldAddComment() throws InterruptedException {
        User user1 = createUser(1);
        User user2 = createUser(2);
        User user3 = createUser(3);
        Item item = createItem(1L, user1.getId(), null);

        BookingDto bookingDto1 = BookingDto.builder()
                .id(1L)
                .itemId(item.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();


        bookingService.create(user2.getId(), bookingDto1);

        BookingDto bookingDto2 = BookingDto.builder()
                .id(2L)
                .itemId(item.getId())
                .bookerId(user3.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();


        bookingService.create(user3.getId(), bookingDto2);


        Thread.sleep(3_000);

        CommentDto commentDto1 = CommentDto.builder()
                .id(1L)
                .text("Good item")
                .itemId(item.getId())
                .authorName(user2.getName())
                .created(LocalDateTime.now())
                .build();

        CommentDto createdCommentDto1 = itemService.addComment(user2.getId(), item.getId(), commentDto1);

        assertThat(createdCommentDto1, notNullValue());
        assertThat(createdCommentDto1.getText(), equalTo(commentDto1.getText()));


        CommentDto commentDto2 = CommentDto.builder()
                .id(2L)
                .text("Very good item")
                .itemId(item.getId())
                .authorName(user3.getName())
                .created(LocalDateTime.now())
                .build();

        CommentDto createdCommentDto2 = itemService.addComment(user3.getId(), item.getId(), commentDto2);

        assertThat(createdCommentDto2, notNullValue());
        assertThat(createdCommentDto2.getText(), equalTo(commentDto2.getText()));

        List<CommentDto> checkCommentsList = new ArrayList<>();
        checkCommentsList.add(createdCommentDto1);
        checkCommentsList.add(commentDto2);

        ItemDetailedDto itemDetailedDto = itemService.getByUserIdAndItemId(user1.getId(), item.getId());

        assertThat(itemDetailedDto.getComments().size(), is(2));
        assertThat(itemDetailedDto.getComments().get(0).getText(), equalTo(checkCommentsList.get(0).getText()));
        assertThat(itemDetailedDto.getComments().get(1).getText(), equalTo(checkCommentsList.get(1).getText()));

        assertThrows(NotFoundException.class, () -> itemService.addComment(4L, item.getId(), commentDto1),
                "Пользователь с id 4 не найден");

        assertThrows(NotFoundException.class, () -> itemService.addComment(user2.getId(), 2L, commentDto1),
                "Предмет с id 2 не найден");
        createUser(4);
        assertThrows(ValidationException.class, () -> itemService.addComment(4L, item.getId(), commentDto1),
                "Пользователь с id 4 не может оставить комментарий," +
                        " т.к. он не арендовал предмет с id " + item.getId());

//        commentDto1.setText("");
//        assertThrows(ValidationException.class, () -> itemService.addComment(user2.getId(), item.getId(), commentDto1),
//                "Необходимо ввести текст комментария");

        Item item2 = createItem(2L, user1.getId(), null);
        BookingDto bookingDto3 = BookingDto.builder()
                .id(3L)
                .itemId(item2.getId())
                .bookerId(user3.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.create(user3.getId(), bookingDto3);
        assertThrows(ValidationException.class, () -> itemService.addComment(user3.getId(), item2.getId(), commentDto2),
                "Пользователь не может оставить комментарий, т.к. срок его аренды предмета ещё не закончился");
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
                .name("Item" + itemId)
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
