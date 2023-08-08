package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonClass {
    @Autowired
    JacksonTester<BookingDto> json;

    @Test
    void bookingDtoTest() throws IOException {
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(5);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(localDateTime)
                .end(localDateTime.plusMinutes(5))
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(localDateTime.format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(
                localDateTime.plusMinutes(5).format(formatter));
    }
}
