package com.stadium.booking.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VenueStaffRepository {
    @Select("SELECT venue_id FROM venue_staff WHERE admin_user_id = #{adminUserId} ORDER BY venue_id")
    List<Long> findVenueIdsByAdminUserId(Long adminUserId);

    @Select("SELECT COUNT(*) > 0 FROM venue_staff WHERE admin_user_id = #{adminUserId} AND venue_id = #{venueId}")
    boolean existsByAdminUserIdAndVenueId(@Param("adminUserId") Long adminUserId, @Param("venueId") Long venueId);

    @Delete("DELETE FROM venue_staff WHERE admin_user_id = #{adminUserId}")
    int deleteByAdminUserId(Long adminUserId);

    @Insert("INSERT INTO venue_staff (admin_user_id, venue_id) VALUES (#{adminUserId}, #{venueId})")
    int insertBinding(@Param("adminUserId") Long adminUserId, @Param("venueId") Long venueId);
}
