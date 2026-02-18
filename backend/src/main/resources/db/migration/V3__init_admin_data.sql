INSERT INTO admin_user (username, password_hash, name, status)
VALUES ('admin', '$2a$10$iqcxZ4367ty577aoUJ86uekMz1Ad1dmJVp8laS8uM5aaFLH51Fcuq', '系统管理员', 1);

INSERT INTO admin_user_role (admin_user_id, role_id)
SELECT au.id, r.id FROM admin_user au, role r 
WHERE au.username = 'admin' AND r.code = 'ADMIN';
