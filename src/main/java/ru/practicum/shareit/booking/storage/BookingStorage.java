package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.start <= ?2" +
            " and b.end > ?2" +
            " order by b.start desc")
    List<Booking> findCurrentBookingsByBookerId(Long bookerId, LocalDateTime timeNow);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.end <= ?2" +
            " order by b.start desc")
    List<Booking> findByBookerIdAndEndInPast(Long bookerId, LocalDateTime endTime);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.start > ?2" +
            " order by b.start desc")
    List<Booking> findByBookerIdAndStartInFuture(Long bookerId, LocalDateTime startTime);

    @Query("select b" +
            " from Booking as b" +
            " join b.booker as u" +
            " where u.id = ?1" +
            " and b.status = ?2" +
            " order by b.start desc")
    List<Booking> findByBookerIdAndStatusContaining(Long bookerId, Status status);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " order by b.start desc")
    List<Booking> getAllBookingsForOwnersItems(Long userId);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.start <= ?2" +
            " and b.end > ?3" +
            " order by b.start desc")
    List<Booking> getCurrentBookingsForOwnersItems(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.end <= ?2" +
            " order by b.start desc")
    List<Booking> getPastBookingsForOwnersItems(Long userId, LocalDateTime endTime);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.start > ?2" +
            " order by b.start desc")
    List<Booking> getFutureBookingsForOwnersItems(Long userId, LocalDateTime startTime);

    @Query("select b" +
            " from Booking as b" +
            " join b.item as i" +
            " join i.owner as o" +
            " where o.id = ?1" +
            " and b.status = ?2" +
            " order by b.start desc")
    List<Booking> getBookingsForOwnersWithStatusContaining(Long userId, Status status);

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
