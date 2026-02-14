package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.ViolationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ViolationRecordRepository extends BaseMapper<ViolationRecord> {
    @Select("""
        SELECT COUNT(*) FROM violation_record
        WHERE user_id = #{userId}
        AND booking_date >= #{startDate}
        AND cleared_at IS NULL
        AND deleted_at IS NULL
        """)
    int countRecentViolations(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("""
        SELECT * FROM violation_record
        WHERE user_id = #{userId}
        AND deleted_at IS NULL
        ORDER BY marked_at DESC
        """)
    List<ViolationRecord> findByUserId(Long userId);

    @Select("""
        SELECT * FROM violation_record
        WHERE booking_id = #{bookingId}
        AND deleted_at IS NULL
        """)
    ViolationRecord findByBookingId(Long bookingId);

    @Update("""
        UPDATE violation_record
        SET cleared_at = NOW(), cleared_by = #{clearedBy}
        WHERE id = #{id} AND cleared_at IS NULL
        """)
    int clearViolation(@Param("id") Long id, @Param("clearedBy") Long clearedBy);
}
