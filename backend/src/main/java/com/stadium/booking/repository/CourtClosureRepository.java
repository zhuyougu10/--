package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.CourtClosure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CourtClosureRepository extends BaseMapper<CourtClosure> {
    @Select("""
        SELECT * FROM court_closure 
        WHERE court_id = #{courtId} 
        AND end_time > #{startTime} 
        AND start_time < #{endTime}
        """)
    List<CourtClosure> findOverlapping(Long courtId, LocalDateTime startTime, LocalDateTime endTime);
}
