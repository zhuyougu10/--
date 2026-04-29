package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.request.BookingCreateRequest;
import com.stadium.booking.entity.User;
import com.stadium.booking.entity.Venue;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingValidatorTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingValidator bookingValidator;

    @Test
    void acceptsStartTimeAlignedToOpenTimeFor60MinuteSlots() {
        mockCommonDependencies(buildVenue(LocalTime.of(7, 30), LocalTime.of(12, 30), 60));
        mockBookingLimitCounts();

        assertDoesNotThrow(() -> bookingValidator.validateBooking(1L,
                buildRequest(LocalTime.of(7, 30), LocalTime.of(8, 30))));
    }

    @Test
    void rejectsStartTimeNotAlignedToOpenTimeFor60MinuteSlots() {
        mockCommonDependencies(buildVenue(LocalTime.of(7, 30), LocalTime.of(12, 30), 60));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingValidator.validateBooking(1L,
                        buildRequest(LocalTime.of(8, 0), LocalTime.of(9, 0))));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertEquals("开始时间必须对齐时段边界", exception.getMessage());
    }

    @Test
    void acceptsMultiSlotDurationWhenStartIsOpenTimeAligned() {
        mockCommonDependencies(buildVenue(LocalTime.of(7, 30), LocalTime.of(12, 30), 60));
        mockBookingLimitCounts();

        assertDoesNotThrow(() -> bookingValidator.validateBooking(1L,
                buildRequest(LocalTime.of(7, 30), LocalTime.of(9, 30))));
    }

    @Test
    void accepts90MinuteSlotGridAnchoredToOpenTime() {
        mockCommonDependencies(buildVenue(LocalTime.of(7, 30), LocalTime.of(12, 0), 90));
        mockBookingLimitCounts();

        assertDoesNotThrow(() -> bookingValidator.validateBooking(1L,
                buildRequest(LocalTime.of(9, 0), LocalTime.of(10, 30))));
    }

    @Test
    void rejectsDurationNotMultipleOfSlotMinutes() {
        mockCommonDependencies(buildVenue(LocalTime.of(7, 30), LocalTime.of(12, 30), 60));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingValidator.validateBooking(1L,
                        buildRequest(LocalTime.of(7, 30), LocalTime.of(9, 0))));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertEquals("时段长度必须为 60 分钟的整数倍", exception.getMessage());
    }

    private void mockCommonDependencies(Venue venue) {
        User user = new User();
        user.setId(1L);
        user.setStatus(1);

        when(venueRepository.findById(2L)).thenReturn(Optional.of(venue));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    private void mockBookingLimitCounts() {
        LocalDate bookingDate = venueBookingDate();
        LocalDate weekStart = bookingDate.minusDays(bookingDate.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        when(bookingRepository.countSlotsByUserAndDate(1L, bookingDate)).thenReturn(0);
        when(bookingRepository.countSlotsByUserAndDateRange(1L, weekStart, weekEnd)).thenReturn(0);
    }

    private BookingCreateRequest buildRequest(LocalTime startTime, LocalTime endTime) {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setVenueId(2L);
        request.setCourtId(3L);
        request.setBookingDate(venueBookingDate());
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        return request;
    }

    private Venue buildVenue(LocalTime openTime, LocalTime closeTime, int slotMinutes) {
        Venue venue = new Venue();
        venue.setId(2L);
        venue.setStatus(1);
        venue.setOpenDays("1,2,3,4,5,6,7");
        venue.setOpenTime(openTime);
        venue.setCloseTime(closeTime);
        venue.setSlotMinutes(slotMinutes);
        venue.setBookAheadDays(7);
        venue.setDailySlotLimit(10);
        venue.setWeeklySlotLimit(20);
        return venue;
    }

    private LocalDate venueBookingDate() {
        return LocalDate.now().plusDays(1);
    }
}
