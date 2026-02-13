package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE openid = #{openid} AND deleted_at IS NULL")
    Optional<User> findByOpenid(String openid);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted_at IS NULL")
    Optional<User> findById(Long id);
}
