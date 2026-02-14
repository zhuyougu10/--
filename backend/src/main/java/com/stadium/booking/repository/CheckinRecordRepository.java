package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface CheckinRecordRepository extends BaseMapper<CheckinRecord> {
    @Select("SELECT * FROM checkin_record WHERE booking_id = #{bookingId}")
    Optional<CheckinRecord> findByBookingId(Long bookingId);
}
