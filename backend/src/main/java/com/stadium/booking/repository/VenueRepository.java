package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.Venue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface VenueRepository extends BaseMapper<Venue> {
    @Select("SELECT * FROM venue WHERE deleted_at IS NULL AND status = 1 ORDER BY id")
    List<Venue> findAllActive();

    @Select("SELECT * FROM venue WHERE id = #{id} AND deleted_at IS NULL")
    Optional<Venue> findById(Long id);

    @Select("SELECT * FROM venue WHERE sport_type = #{sportType} AND deleted_at IS NULL AND status = 1")
    List<Venue> findBySportType(String sportType);

    @Select("SELECT * FROM venue WHERE code = #{code} AND deleted_at IS NULL")
    Optional<Venue> findByCode(String code);

    @Select("SELECT MAX(code) FROM venue WHERE code LIKE #{prefix} AND deleted_at IS NULL")
    String findMaxCodeByPrefix(String prefix);
}
