package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

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
}
