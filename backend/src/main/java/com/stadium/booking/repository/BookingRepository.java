package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface BookingRepository extends BaseMapper<Booking> {
    @Select("""
        SELECT * FROM booking 
        WHERE court_id = #{courtId} 
        AND booking_date = #{date} 
        AND status = 1
        AND deleted_at IS NULL
        ORDER BY start_time
        """)
    List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date);

    @Select("""
        SELECT * FROM booking 
        WHERE id = #{id} 
        AND deleted_at IS NULL
        """)
    Optional<Booking> findById(Long id);

    @Select("""
        SELECT * FROM booking 
        WHERE booking_no = #{bookingNo} 
        AND deleted_at IS NULL
        """)
    Optional<Booking> findByBookingNo(String bookingNo);

    @Select("""
        SELECT * FROM booking 
        WHERE user_id = #{userId} 
        AND deleted_at IS NULL
        ORDER BY booking_date DESC, start_time DESC
        """)
    List<Booking> findByUserId(Long userId);

    @Select("""
        SELECT COALESCE(SUM(slot_count), 0) FROM booking 
        WHERE user_id = #{userId} 
        AND booking_date = #{date} 
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countSlotsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("""
        SELECT COALESCE(SUM(slot_count), 0) FROM booking 
        WHERE user_id = #{userId} 
        AND booking_date BETWEEN #{startDate} AND #{endDate}
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countSlotsByUserAndDateRange(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    @Select("""
        SELECT COUNT(*) FROM booking 
        WHERE court_id = #{courtId} 
        AND booking_date = #{date}
        AND start_time < #{endTime}
        AND end_time > #{startTime}
        AND status = 1
        AND deleted_at IS NULL
        """)
    int countConflictingBookings(
        @Param("courtId") Long courtId,
        @Param("date") LocalDate date,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime
    );

    @Update("""
        UPDATE booking 
        SET status = 2, cancel_reason = #{reason}, cancelled_at = NOW(), 
            cancelled_by = #{cancelledBy}, cancelled_by_type = #{cancelledByType}
        WHERE id = #{id}
        """)
    int cancelBooking(
        @Param("id") Long id, 
        @Param("reason") String reason,
        @Param("cancelledBy") Long cancelledBy,
        @Param("cancelledByType") Integer cancelledByType
    );

    @Select("""
        SELECT * FROM booking 
        WHERE venue_id = #{venueId} 
        AND booking_date = #{date}
        AND status IN (1, 3)
        AND deleted_at IS NULL
        ORDER BY court_id, start_time
        """)
    List<Booking> findByVenueAndDate(Long venueId, LocalDate date);

    @Select("""
        SELECT * FROM booking 
        WHERE status = 1 
        AND booking_date = #{date}
        AND deleted_at IS NULL
        ORDER BY venue_id, court_id, start_time
        """)
    List<Booking> findTodayBookings(LocalDate date);

    @Select("""
        SELECT * FROM booking 
        WHERE user_id = #{userId} 
        AND status = #{status}
        AND deleted_at IS NULL
        ORDER BY booking_date DESC, start_time DESC
        """)
    List<Booking> findByUserIdAndStatus(Long userId, Integer status);
}
