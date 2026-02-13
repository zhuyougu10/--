package com.stadium.booking.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RolePermissionRepository {
    @Select("""
        SELECT COUNT(*) > 0 FROM role_permission rp
        JOIN admin_user_role aur ON aur.role_id = rp.role_id
        JOIN permission p ON p.id = rp.permission_id
        WHERE aur.admin_user_id = #{adminUserId} AND p.code = #{permissionCode}
        """)
    boolean hasPermission(Long adminUserId, String permissionCode);
}
