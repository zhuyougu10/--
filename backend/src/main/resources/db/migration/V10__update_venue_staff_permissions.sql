UPDATE role
SET name = '场馆管理员', description = '负责已授权球馆的运营管理'
WHERE code = 'VENUE_STAFF';

DELETE rp FROM role_permission rp
JOIN role r ON r.id = rp.role_id
JOIN permission p ON p.id = rp.permission_id
WHERE r.code = 'VENUE_STAFF' AND p.code = 'stats:read';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p
WHERE r.code = 'VENUE_STAFF'
AND p.code IN ('venue:update', 'court:create', 'court:update', 'court:delete', 'booking:cancel');
