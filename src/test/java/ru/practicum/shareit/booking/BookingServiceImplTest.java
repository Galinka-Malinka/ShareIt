package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForAnswerDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
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
public class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void shouldCreateBooking() {
        User user1 = createUser(1);
        Item item = createItem(1L, user1.getId(), null);
        User user2 = createUser(2);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(item.getId())
                .bookerId(user2.getId())
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        bookingService.create(user2.getId(), bookingDto);

        TypedQuery<Booking> queryForItem = em.createQuery("Select b from Booking b where b.id = :id",
                Booking.class);
        Booking booking = queryForItem.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(user2));
        assertThat(booking.getStatus(), equalTo(Status.WAITING));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingDto),
                "Данный предмет не доступен для бронирования," +
                        " т.к. пользователь не может забронировать собственный предмет");
        assertThrows(NotFoundException.class, () -> bookingService.create(3L, bookingDto),
                "Пользователь с id 3 не найден");

        bookingDto.setStart(null);

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Необходимо указать дату начала аренды");
        bookingDto.setStart(LocalDateTime.now().minusMinutes(1));
        bookingDto.setEnd(null);

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Необходимо указать дату окончания аренды");
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Окончание аренды не может быть в прошлом");
        bookingDto.setEnd(LocalDateTime.now().plusMinutes(1));

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Начало аренды не может быть в прошлом");
        bookingDto.setStart(LocalDateTime.now().plusMinutes(5));

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Окончание аренды не может быть раньше её начала");

        LocalDateTime time = LocalDateTime.now().plusSeconds(1);
        bookingDto.setStart(time);
        bookingDto.setEnd(time);

        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Дата начала аренды должна отличаться от даты окончания аренды");

        item.setAvailable(false);
        assertThrows(ValidationException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Предмет с id " + bookingDto.getItemId() + " в данный момент не доступен для аренды");

        bookingDto.setItemId(2L);
        assertThrows(NotFoundException.class, () -> bookingService.create(user2.getId(), bookingDto),
                "Предмет с id " + bookingDto.getItemId() + " не найден");
    }

    @Test
    void shouldRespondToBookingRequest() {
        User user1 = createUser(1);
        Item item = createItem(1L, user1.getId(), null);
        User user2 = createUser(2);

        Booking booking = createBooking(1L, item.getId(), user2.getId());

        bookingService.respondToBookingRequest(user1.getId(), booking.getId(), true);

        TypedQuery<Booking> queryForItem = em.createQuery("Select b from Booking b where b.id = :id",
                Booking.class);
        Booking bookingAfterConfirmation = queryForItem.setParameter("id", booking.getId()).getSingleResult();

        assertThat(bookingAfterConfirmation.getId(), notNullValue());
        assertThat(bookingAfterConfirmation.getStart(), equalTo(booking.getStart()));
        assertThat(bookingAfterConfirmation.getEnd(), equalTo(booking.getEnd()));
        assertThat(bookingAfterConfirmation.getItem(), equalTo(item));
        assertThat(bookingAfterConfirmation.getBooker(), equalTo(user2));
        assertThat(bookingAfterConfirmation.getStatus(), equalTo(Status.APPROVED));

        Booking booking2 = createBooking(2L, item.getId(), user2.getId());

        bookingService.respondToBookingRequest(user1.getId(), booking2.getId(), false);

        TypedQuery<Booking> queryForItem2 = em.createQuery("Select b from Booking b where b.id = :id",
                Booking.class);
        Booking bookingAfterRefusal = queryForItem2.setParameter("id", booking2.getId()).getSingleResult();

        assertThat(bookingAfterRefusal.getId(), notNullValue());
        assertThat(bookingAfterRefusal.getStart(), equalTo(booking2.getStart()));
        assertThat(bookingAfterRefusal.getEnd(), equalTo(booking2.getEnd()));
        assertThat(bookingAfterRefusal.getItem(), equalTo(item));
        assertThat(bookingAfterRefusal.getBooker(), equalTo(user2));
        assertThat(bookingAfterRefusal.getStatus(), equalTo(Status.REJECTED));

        assertThrows(NotFoundException.class,
                () -> bookingService.respondToBookingRequest(3L, booking.getId(), true),
                "Пользователь с id 3 не найден");

        assertThrows(NotFoundException.class,
                () -> bookingService.respondToBookingRequest(user2.getId(), 3L, true),
                "Бронирование с id 3 не найдено");

        assertThrows(NotFoundException.class,
                () -> bookingService.respondToBookingRequest(user2.getId(), booking.getId(), true),
                "Пользователь с id " + user2.getId() + "не является владельцем вещи с id ");

        assertThrows(ValidationException.class,
                () -> bookingService.respondToBookingRequest(user1.getId(), booking.getId(), true),
                "У предмета с id " + item.getId()
                        + " уже установлен статус возможности бронирования");
    }

    @Test
    void shouldGetByUserIdAndBookingId() {
        User user1 = createUser(1);
        Item item = createItem(1L, user1.getId(), null);
        User user2 = createUser(2);
        User user3 = createUser(3);

        Booking booking = createBooking(1L, item.getId(), user2.getId());

        BookingForAnswerDto bookingForAnswerDto = bookingService
                .getByUserIdAndBookingId(user2.getId(), booking.getId());

        assertThat(bookingForAnswerDto, notNullValue());
        assertThat(bookingForAnswerDto, equalTo(BookingMapper.toBookingForAnswerDto(booking)));

        assertThrows(NotFoundException.class,
                () -> bookingService.getByUserIdAndBookingId(4L, booking.getId()),
                "Пользователь с id 4 не найден");

        assertThrows(NotFoundException.class,
                () -> bookingService.getByUserIdAndBookingId(user3.getId(), booking.getId()),
                "Пользователь с id " + user3.getId() + "не имеет доступ к бронированию с id "
                        + booking.getId());

        assertThrows(NotFoundException.class,
                () -> bookingService.getByUserIdAndBookingId(user2.getId(), 2L),
                "Бронирование с id 2 не найдено");
    }

    @Test
    void shouldGetBookingsByUser() throws InterruptedException {
        User user1 = createUser(1);
        Item item = createItem(1L, user1.getId(), null);
        User user2 = createUser(2);
        User user3 = createUser(3);

        Booking booking1 = createBooking(1L, item.getId(), user2.getId());
        Booking booking2 = createBooking(2L, item.getId(), user2.getId());
        Booking booking3 = createBooking(3L, item.getId(), user2.getId());
        Booking booking4 = createBooking(4L, item.getId(), user2.getId());
        createBooking(5L, item.getId(), user3.getId());

        List<BookingForAnswerDto> checkList1 = new ArrayList<>();
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking4));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking3));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking2));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking1));

        List<BookingForAnswerDto> list1 = bookingService.getBookingsByUser(user2.getId(), "ALL", 0, 10);

        assertThat(list1, notNullValue());
        assertThat(list1, equalTo(checkList1));

        List<BookingForAnswerDto> checkList2 = new ArrayList<>();
        checkList2.add(BookingMapper.toBookingForAnswerDto(booking1));

        List<BookingForAnswerDto> list2 = bookingService.getBookingsByUser(user2.getId(), "ALL", 3, 3);
        assertThat(list2, notNullValue());
        assertThat(list2, equalTo(checkList2));

        Thread.sleep(2_000);

        List<BookingForAnswerDto> list3 = bookingService
                .getBookingsByUser(user2.getId(), "CURRENT", 0, 10);
        assertThat(list3.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking2)));

        List<BookingForAnswerDto> list4 = bookingService
                .getBookingsByUser(user2.getId(), "PAST", 0, 10);
        assertThat(list4.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking1)));

        List<BookingForAnswerDto> list5 = bookingService
                .getBookingsByUser(user2.getId(), "FUTURE", 0, 10);
        assertThat(list5.size(), is(2));
        assertThat(list5.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking4)));
        assertThat(list5.get(1), equalTo(BookingMapper.toBookingForAnswerDto(booking3)));

        List<BookingForAnswerDto> list6 = bookingService
                .getBookingsByUser(user2.getId(), "WAITING", 0, 10);
        assertThat(list6, equalTo(checkList1));

        bookingService.respondToBookingRequest(user1.getId(), booking4.getId(), false);
        List<BookingForAnswerDto> list7 = bookingService
                .getBookingsByUser(user2.getId(), "REJECTED", 0, 10);
        assertThat(list7.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking4)));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingsByUser(user2.getId(), "ALL", -1, 10),
                "from не может быть меньше 0");

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingsByUser(user2.getId(), "ALL", 1, 0),
                "size не может быть меньше 1");

        assertThrows(ValidationException.class,
                () -> bookingService.getBookingsByUser(user2.getId(), "EveryThing", 0, 20),
                "Unknown state: EveryThing");


        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByUser(4L, "ALL", 3, 3),
                "Пользователь с id 4 не найден");
    }

    @Test
    void shouldGetBookingsForOwnersItems() throws InterruptedException {
        User user1 = createUser(1);
        Item item1 = createItem(1L, user1.getId(), null);
        Item item2 = createItem(2L, user1.getId(), null);
        Item item3 = createItem(3L, user1.getId(), null);
        Item item4 = createItem(4L, user1.getId(), null);
        Item item5 = createItem(5L, user1.getId(), null);

        User user2 = createUser(2);
        Booking booking1 = createBooking(1L, item1.getId(), user2.getId());
        Booking booking2 = createBooking(2L, item2.getId(), user2.getId());
        Booking booking3 = createBooking(3L, item3.getId(), user2.getId());
        Booking booking4 = createBooking(4L, item4.getId(), user2.getId());
        Booking booking5 = createBooking(5L, item5.getId(), user2.getId());

        List<BookingForAnswerDto> checkList1 = new ArrayList<>();
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking5));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking4));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking3));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking2));
        checkList1.add(BookingMapper.toBookingForAnswerDto(booking1));

        List<BookingForAnswerDto> list1 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "ALL", 0, 10);

        assertThat(list1, notNullValue());
        assertThat(list1, equalTo(checkList1));

        List<BookingForAnswerDto> checkList2 = new ArrayList<>();
        checkList2.add(BookingMapper.toBookingForAnswerDto(booking1));

        List<BookingForAnswerDto> list2 = bookingService.getBookingsByUser(user2.getId(), "ALL", 5, 2);
        assertThat(list2, notNullValue());
        assertThat(list2, equalTo(checkList2));

        Thread.sleep(2_000);

        List<BookingForAnswerDto> list3 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "CURRENT", 0, 10);
        assertThat(list3.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking2)));

        List<BookingForAnswerDto> list4 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "PAST", 0, 10);
        assertThat(list4.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking1)));

        List<BookingForAnswerDto> list5 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "FUTURE", 0, 10);
        assertThat(list5.size(), is(3));
        assertThat(list5.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking5)));
        assertThat(list5.get(1), equalTo(BookingMapper.toBookingForAnswerDto(booking4)));
        assertThat(list5.get(2), equalTo(BookingMapper.toBookingForAnswerDto(booking3)));

        List<BookingForAnswerDto> list6 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "WAITING", 0, 10);
        assertThat(list6, equalTo(checkList1));

        bookingService.respondToBookingRequest(user1.getId(), booking4.getId(), false);
        List<BookingForAnswerDto> list7 = bookingService
                .getBookingsForOwnersItems(user1.getId(), "REJECTED", 0, 10);
        assertThat(list7.get(0), equalTo(BookingMapper.toBookingForAnswerDto(booking4)));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingsForOwnersItems(user1.getId(), "ALL", -1, 10),
                "from не может быть меньше 0");

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingsForOwnersItems(user1.getId(), "ALL", 1, 0),
                "size не может быть меньше 1");

        assertThrows(ValidationException.class,
                () -> bookingService.getBookingsForOwnersItems(user1.getId(), "EveryThing", 0, 20),
                "Unknown state: EveryThing");

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsForOwnersItems(user2.getId(), "ALL", 0, 20),
                "У пользователя с id " + user2.getId() + " нет предметов для шеринга");

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByUser(3L, "ALL", 5, 2),
                "Пользователь с id 3 не найден");
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

    public Booking createBooking(Long bookingId, Long itemId, Long userId) {
        BookingDto bookingDto = BookingDto.builder()
                .id(bookingId)
                .itemId(itemId)
                .bookerId(userId)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(bookingId))
                .end(LocalDateTime.now().plusSeconds(bookingId + 1))
                .build();

        bookingService.create(userId, bookingDto);

        TypedQuery<Booking> queryForItem = em.createQuery("Select b from Booking b where b.id = :id",
                Booking.class);
        return queryForItem.setParameter("id", bookingDto.getId()).getSingleResult();
    }
}
