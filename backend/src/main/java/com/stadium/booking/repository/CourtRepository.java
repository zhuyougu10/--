package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Court;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CourtRepository extends BaseMapper<Court> {
    @Select("SELECT * FROM court WHERE venue_id = #{venueId} AND deleted_at IS NULL ORDER BY sort_order, id")
    List<Court> findByVenueId(Long venueId);

    @Select("SELECT * FROM court WHERE id = #{id} AND deleted_at IS NULL")
    Optional<Court> findById(Long id);

    @Select("SELECT * FROM court WHERE venue_id = #{venueId} AND status = 1 AND deleted_at IS NULL ORDER BY sort_order, id")
    List<Court> findActiveByVenueId(Long venueId);

    @Select("SELECT COUNT(*) FROM court WHERE venue_id = #{venueId} AND deleted_at IS NULL")
    int countByVenueId(Long venueId);

    @Select("SELECT MAX(court_no) FROM court WHERE venue_id = #{venueId}")
    String findMaxCourtNoByVenueId(Long venueId);

    @Select("SELECT * FROM court WHERE venue_id = #{venueId} AND name = #{name} AND deleted_at IS NULL LIMIT 1")
    Optional<Court> findByVenueIdAndName(Long venueId, String name);
}
