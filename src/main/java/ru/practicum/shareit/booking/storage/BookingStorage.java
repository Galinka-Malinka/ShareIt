package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long>,
        PagingAndSortingRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long bookerId, Pageable pageable);


    List<Booking> findAllByBookerId(Long bookerId);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.start <= ?2" +
            " and b.end > ?2")
    List<Booking> findCurrentBookingsByBookerId(Long bookerId, LocalDateTime timeNow, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.end <= ?2")
    List<Booking> findByBookerIdAndEndInPast(Long bookerId, LocalDateTime endTime, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.start > ?2")
    List<Booking> findByBookerIdAndStartInFuture(Long bookerId, LocalDateTime startTime, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.status = ?2")
    List<Booking> findByBookerIdAndStatusContaining(Long bookerId, Status status, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1")
    List<Booking> getAllBookingsForOwnersItems(Long userId, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.start <= ?2" +
            " and b.end > ?2")
    List<Booking> getCurrentBookingsForOwnersItems(Long userId, LocalDateTime startTime,
                                                   Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.end <= ?2")
    List<Booking> getPastBookingsForOwnersItems(Long userId, LocalDateTime endTime, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.start > ?2")
    List<Booking> getFutureBookingsForOwnersItems(Long userId, LocalDateTime startTime, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.status = ?2")
    List<Booking> getBookingsForOwnersWithStatusContaining(Long userId, Status status, Pageable pageable);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " where i.id = ?1" +
            " order by b.start desc")
    List<Booking> findByItemIdOrderByStart(Long itemId);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and i.id = ?2" +
            " order by b.start desc")
    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);
}
