package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemStorageTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(entityManager);
    }

    @Test
    public void shouldSaveItem() {
        User user = createUser(1L);

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description for Item")
                .owner(user)
                .available(true)
                .request(null)
                .bookings(new HashSet<>())
                .comments(new HashSet<>())
                .build();

        itemStorage.save(item);

        TypedQuery<Item> queryForItem = entityManager.getEntityManager()
                .createQuery("Select i from Item i where i.id = :id",
                        Item.class);
        Item foundItem = queryForItem.setParameter("id", item.getId()).getSingleResult();

        assertThat(foundItem, equalTo(item));
    }

    @Test
    public void whenGetItemOnRequest_thenReturnListItems() {
        User user = createUser(1L);
        Item item1 = createItem(1L, user);
        Item item2 = createItem(2L, user);
        Item item = Item.builder()
                .id(3L)
                .name("Name")
                .description("Description")
                .owner(user)
                .available(true)
                .request(null)
                .bookings(new HashSet<>())
                .comments(new HashSet<>())
                .build();
        itemStorage.save(item);

        List<Item> items = itemStorage.getItemOnRequest("item",
                PageRequest.of(0, 10, Sort.by("id").ascending()));
        assertThat(items.size(), is(2));
        assertThat(items.get(0), equalTo(item1));
        assertThat(items.get(1), equalTo(item2));
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
}
