package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long>, PagingAndSortingRepository<Item, Long> {

    List<Item> findByOwnerIdOrderById(Long userId, Pageable pageable);


    @Query("select i" +
            " from Item as i" +
            " where (lower(i.name) like lower(concat('%', ?1, '%')) " +
            " or lower(i.description) like lower(concat('%', ?1, '%')))" +
            " and i.available = true")
    List<Item> getItemOnRequest(String text, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findByOwnerId(Long userId);
}
