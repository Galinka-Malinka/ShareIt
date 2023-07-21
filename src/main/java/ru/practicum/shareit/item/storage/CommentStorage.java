package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {

    @Transactional
    @Query("select c" +
            " from Comment as c" +
            " join c.item as i" +
            " where i.id = ?1")
    List<Comment> findByItemId(Long itemId);
}
