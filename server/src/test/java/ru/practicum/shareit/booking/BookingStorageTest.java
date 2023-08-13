package ru.practicum.shareit.booking;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingStorageTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingStorage bookingStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(entityManager);
    }

    @Test
    public void shouldSaveBooking() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        User user = createUser(2L);

        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusSeconds(1))
                .build();

        bookingStorage.save(booking);

        TypedQuery<Booking> query = entityManager.getEntityManager().createQuery(
                "select b from Booking b where b.id = :id",
                Booking.class);
        Booking foundBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(foundBooking, equalTo(booking));
    }

    @Test
    public void whenFindCurrentBookingsByBookerId_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        User user = createUser(2L);
        Booking booking1 = createBooking(1L, item, user);
        createBooking(2L, item, user);
        Item item2 = createItem(2L, owner);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.findCurrentBookingsByBookerId(user.getId(),
                LocalDateTime.now().plusSeconds(1),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking3));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenFindByBookerIdAndEndInPast_thenReturnListBookings() throws InterruptedException {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(2L, item, user);

        Thread.sleep(3_000);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.findByBookerIdAndEndInPast(user.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenFindByBookerIdAndStartInFuture_thenReturnListBookings() throws InterruptedException {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(20L, item, user);

        Thread.sleep(3_000);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.findByBookerIdAndStartInFuture(user.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking3));
    }

    @Test
    public void whenFindByBookerIdAndStatusContaining_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(20L, item, user);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.REJECTED)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> foundWaiting = bookingStorage.findByBookerIdAndStatusContaining(user.getId(),
                Status.WAITING,
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(foundWaiting.size(), is(2));
        assertThat(foundWaiting.get(0), equalTo(booking2));
        assertThat(foundWaiting.get(1), equalTo(booking1));

        List<Booking> foundRejected = bookingStorage.findByBookerIdAndStatusContaining(user.getId(),
                Status.REJECTED,
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(foundRejected.size(), is(1));
        assertThat(foundRejected.get(0), equalTo(booking3));
    }

    @Test
    public void whenGetAllBookingsForOwnersItems_thenReturnListBookings() {
        User user1 = createUser(1L);
        Item item1 = createItem(1L, user1);
        Item item2 = createItem(2L, user1);
        User user2 = createUser(2L);

        Booking booking1 = createBooking(1L, item1, user2);
        Booking booking2 = createBooking(2L, item2, user2);

        Item item3 = createItem(3L, user2);
        Booking booking3 = createBooking(3L, item3, user1);

        List<Booking> found = bookingStorage.getAllBookingsForOwnersItems(user1.getId(),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenGetCurrentBookingsForOwnersItems_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        User user = createUser(2L);
        Booking booking1 = createBooking(1L, item, user);
        createBooking(2L, item, user);
        Item item2 = createItem(2L, owner);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.getCurrentBookingsForOwnersItems(owner.getId(),
                LocalDateTime.now().plusSeconds(1),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking3));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenGetPastBookingsForOwnersItems_thenReturnListBookings() throws InterruptedException {
        User owner = createUser(1L);
        Item item1 = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item1, user);
        Booking booking2 = createBooking(2L, item1, user);

        Thread.sleep(3_000);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.getPastBookingsForOwnersItems(owner.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenGetFutureBookingsForOwnersItems_thenReturnListBookings() throws InterruptedException {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        createBooking(1L, item, user);
        Booking booking2 = createBooking(20L, item, user);

        Thread.sleep(3_000);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> found = bookingStorage.getFutureBookingsForOwnersItems(owner.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking3));
    }

    @Test
    public void whenGetBookingsForOwnersWithStatusContaining_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(2L, item, user);

        Booking bookingForBuilding = Booking.builder()
                .id(3L)
                .item(item2)
                .booker(user)
                .status(Status.REJECTED)
                .start(LocalDateTime.now().plusSeconds(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking3 = bookingStorage.save(bookingForBuilding);

        List<Booking> foundWaiting = bookingStorage.getBookingsForOwnersWithStatusContaining(owner.getId(),
                Status.WAITING,
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(foundWaiting.size(), is(2));
        assertThat(foundWaiting.get(0), equalTo(booking2));
        assertThat(foundWaiting.get(1), equalTo(booking1));

        List<Booking> foundRejected = bookingStorage.getBookingsForOwnersWithStatusContaining(owner.getId(),
                Status.REJECTED,
                PageRequest.of(0, 10, Sort.by("start").descending()));

        assertThat(foundRejected.size(), is(1));
        assertThat(foundRejected.get(0), equalTo(booking3));
    }

    @Test
    public void whenFindByItemIdOrderByStart_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        Item item2 = createItem(2L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(2L, item, user);
        createBooking(3L, item2, user);

        List<Booking> found = bookingStorage.findByItemIdOrderByStart(item.getId());

        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking1));
    }

    @Test
    public void whenFindByBookerIdAndItemId_thenReturnListBookings_thenReturnListBookings() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner);
        User user = createUser(2L);

        Booking booking1 = createBooking(1L, item, user);
        Booking booking2 = createBooking(2L, item, user);

        List<Booking> found = bookingStorage.findByBookerIdAndItemId(user.getId(), item.getId());
        assertThat(found.size(), is(2));
        assertThat(found.get(0), equalTo(booking2));
        assertThat(found.get(1), equalTo(booking1));
    }

    public User createUser(Long id) {
        User user = User.builder()
                .id(id)
                .name("User" + id)
                .email("User" + id + "@mail.ru")
                .build();

        return userStorage.save(user);
    }

    public Item createItem(Long itemId, User owner) {
        Item item = Item.builder()
                .id(itemId)
                .name("Item" + itemId)
                .description("Description for Item" + itemId)
                .owner(owner)
                .available(true)
                .request(null)
                .bookings(new HashSet<>())
                .comments(new HashSet<>())
                .build();
        return itemStorage.save(item);
    }

    public Booking createBooking(Long bookingId, Item item, User user) {
        Booking booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .start(LocalDateTime.now().plusSeconds(bookingId))
                .end(LocalDateTime.now().plusSeconds(bookingId + 1))
                .build();

        return bookingStorage.save(booking);


    }
}
