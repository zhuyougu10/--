package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AdminUserRepository extends BaseMapper<AdminUser> {
    @Select("SELECT * FROM admin_user WHERE username = #{username} AND deleted_at IS NULL")
    Optional<AdminUser> findByUsername(String username);

    @Select("SELECT * FROM admin_user WHERE phone = #{phone} AND deleted_at IS NULL")
    Optional<AdminUser> findByPhone(String phone);

    @Select("SELECT * FROM admin_user WHERE id = #{id} AND deleted_at IS NULL")
    Optional<AdminUser> findById(Long id);

    @Select("SELECT * FROM admin_user WHERE deleted_at IS NULL ORDER BY id")
    List<AdminUser> findAll();
}
