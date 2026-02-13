package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogRepository extends BaseMapper<AuditLog> {
}
