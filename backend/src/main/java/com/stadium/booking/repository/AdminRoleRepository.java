package com.stadium.booking.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminRoleRepository {
    @Select("""
        SELECT r.code
        FROM role r
        JOIN admin_user_role aur ON aur.role_id = r.id
        WHERE aur.admin_user_id = #{adminUserId} AND r.deleted_at IS NULL
        ORDER BY r.id
        """)
    List<String> findRoleCodesByAdminUserId(Long adminUserId);

    @Select("""
        SELECT COUNT(*) > 0
        FROM role r
        JOIN admin_user_role aur ON aur.role_id = r.id
        WHERE aur.admin_user_id = #{adminUserId} AND r.code = #{roleCode} AND r.deleted_at IS NULL
        """)
    boolean hasRole(@Param("adminUserId") Long adminUserId, @Param("roleCode") String roleCode);

    @Insert("""
        INSERT INTO admin_user_role (admin_user_id, role_id)
        SELECT #{adminUserId}, id FROM role WHERE code = #{roleCode} AND deleted_at IS NULL
        """)
    int addRoleToAdminUser(@Param("adminUserId") Long adminUserId, @Param("roleCode") String roleCode);

    @org.apache.ibatis.annotations.Delete("""
        DELETE aur FROM admin_user_role aur
        JOIN role r ON r.id = aur.role_id
        WHERE aur.admin_user_id = #{adminUserId} AND r.code = #{roleCode}
        """)
    int removeRoleFromAdminUser(@Param("adminUserId") Long adminUserId, @Param("roleCode") String roleCode);
}
