package com.stadium.booking.service;

import com.stadium.booking.common.exception.BusinessException;
import com.stadium.booking.common.result.ErrorCode;
import com.stadium.booking.dto.response.BookingResponse;
import com.stadium.booking.entity.Booking;
import com.stadium.booking.repository.BookingRepository;
import com.stadium.booking.repository.CourtRepository;
import com.stadium.booking.repository.UserRepository;
import com.stadium.booking.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingValidator bookingValidator;

    @Mock
    private AuditService auditService;

    @Mock
    private AdminVenueAccessService adminVenueAccessService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void userCanReadOwnBookingByNo() {
        Booking booking = buildBooking(11L, 1L);
        when(bookingRepository.findByBookingNo("BK20260429AAAA1111")).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getUserBookingByNo(11L, "BK20260429AAAA1111");

        assertEquals("BK20260429AAAA1111", response.getBookingNo());
        assertEquals(11L, response.getUserId());
        verify(adminVenueAccessService, never()).checkVenueAccess(1L);
    }

    @Test
    void userCannotReadOthersBookingByNo() {
        Booking booking = buildBooking(11L, 1L);
        when(bookingRepository.findByBookingNo("BK20260429AAAA1111")).thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.getUserBookingByNo(22L, "BK20260429AAAA1111"));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(adminVenueAccessService, never()).checkVenueAccess(1L);
    }

    @Test
    void adminDetailPathKeepsVenueStaffAccessCheck() {
        Booking booking = buildBooking(11L, 1L);
        when(bookingRepository.findByBookingNo("BK20260429AAAA1111")).thenReturn(Optional.of(booking));
        when(adminVenueAccessService.isCurrentVenueStaffRole()).thenReturn(true);

        BookingResponse response = bookingService.getBookingByNo("BK20260429AAAA1111");

        assertEquals("BK20260429AAAA1111", response.getBookingNo());
        verify(adminVenueAccessService).checkVenueAccess(1L);
    }

    private Booking buildBooking(Long userId, Long venueId) {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingNo("BK20260429AAAA1111");
        booking.setUserId(userId);
        booking.setUserName("张三");
        booking.setUserPhone("13800000001");
        booking.setVenueId(venueId);
        booking.setVenueName("体育馆A馆——篮球馆");
        booking.setCourtId(5L);
        booking.setCourtName("1号场");
        booking.setBookingDate(LocalDate.of(2026, 4, 30));
        booking.setStartTime(LocalTime.of(19, 0));
        booking.setEndTime(LocalTime.of(20, 0));
        booking.setSlotCount(1);
        booking.setBookingType(1);
        booking.setStatus(1);
        booking.setRemark("测试预约");
        booking.setCreatedAt(LocalDateTime.of(2026, 4, 29, 21, 0));
        return booking;
    }
}
