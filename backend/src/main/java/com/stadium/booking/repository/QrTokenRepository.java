package com.stadium.booking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stadium.booking.entity.QrToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface QrTokenRepository extends BaseMapper<QrToken> {
    @Select("SELECT * FROM qr_token WHERE token = #{token}")
    Optional<QrToken> findByToken(String token);

    @Select("SELECT * FROM qr_token WHERE booking_id = #{bookingId} AND expires_at > NOW() AND used_at IS NULL ORDER BY id DESC LIMIT 1")
    Optional<QrToken> findActiveByBookingId(Long bookingId);

    @Update("UPDATE qr_token SET used_at = #{usedAt} WHERE id = #{id} AND used_at IS NULL")
    int markAsUsed(@Param("id") Long id, @Param("usedAt") LocalDateTime usedAt);

    @Update("DELETE FROM qr_token WHERE expires_at < #{cutoff}")
    int deleteExpired(LocalDateTime cutoff);
}
