CREATE TABLE campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '校区名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '校区编码',
    address VARCHAR(255) COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL COMMENT '软删除时间'
) ENGINE=InnoDB COMMENT='校区表';

INSERT INTO campus (name, code, address) 
VALUES ('主校区', 'MAIN', '学校主校区');

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    union_id VARCHAR(100) COMMENT '微信unionid',
    name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    student_no VARCHAR(50) COMMENT '学号/工号',
    user_type TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-学生, 2-教师, 3-外部人员',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    banned_until DATETIME COMMENT '禁用截止时间',
    no_show_count INT DEFAULT 0 COMMENT '近30天爽约次数',
    last_no_show_at DATETIME COMMENT '最近爽约时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    INDEX idx_openid (openid),
    INDEX idx_student_no (student_no),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='管理员用户表';

CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统内置角色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    action VARCHAR(50) COMMENT '操作',
    description VARCHAR(255) COMMENT '权限描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB COMMENT='权限表';

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB COMMENT='角色权限关联表';

CREATE TABLE admin_user_role (
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_user_id, role_id),
    FOREIGN KEY (admin_user_id) REFERENCES admin_user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB COMMENT='管理员角色关联表';

CREATE TABLE venue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL COMMENT '所属校区',
    name VARCHAR(100) NOT NULL COMMENT '球馆名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '球馆编码',
    sport_type VARCHAR(50) NOT NULL COMMENT '运动类型: badminton, basketball, table_tennis, etc.',
    location VARCHAR(255) COMMENT '位置描述',
    description TEXT COMMENT '详细描述',
    image_url VARCHAR(255) COMMENT '封面图片',
    open_days VARCHAR(20) DEFAULT '1,2,3,4,5,6,7' COMMENT '开放日(1-7代表周一到周日)',
    open_time TIME DEFAULT '08:00:00' COMMENT '每日开放开始时间',
    close_time TIME DEFAULT '22:00:00' COMMENT '每日开放结束时间',
    slot_minutes INT DEFAULT 60 COMMENT '时段长度(分钟)',
    book_ahead_days INT DEFAULT 7 COMMENT '可提前预约天数',
    cancel_cutoff_minutes INT DEFAULT 30 COMMENT '取消截止时间(开始前分钟)',
    checkin_window_before INT DEFAULT 15 COMMENT '核销窗口开始(开始前分钟)',
    no_show_grace_minutes INT DEFAULT 15 COMMENT '爽约宽限期(开始后分钟)',
    daily_slot_limit INT DEFAULT 2 COMMENT '个人日限额(时段数)',
    weekly_slot_limit INT DEFAULT 10 COMMENT '个人周限额(时段数)',
    group_booking_enabled TINYINT DEFAULT 1 COMMENT '是否允许团体预约',
    group_max_courts INT DEFAULT 4 COMMENT '团体预约最大场地数',
    group_max_hours INT DEFAULT 4 COMMENT '团体预约最大时长(小时)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-开放, 0-关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    INDEX idx_campus (campus_id),
    INDEX idx_sport_type (sport_type),
    INDEX idx_status (status),
    FOREIGN KEY (campus_id) REFERENCES campus(id)
) ENGINE=InnoDB COMMENT='球馆表';

CREATE TABLE venue_staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_venue (admin_user_id, venue_id),
    FOREIGN KEY (admin_user_id) REFERENCES admin_user(id),
    FOREIGN KEY (venue_id) REFERENCES venue(id)
) ENGINE=InnoDB COMMENT='场馆员球馆关联表';

CREATE TABLE court (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL COMMENT '所属球馆',
    name VARCHAR(50) NOT NULL COMMENT '场地名称/编号',
    court_no VARCHAR(20) COMMENT '场地编号(如1号场、A场)',
    sport_type VARCHAR(50) COMMENT '运动类型(可覆盖球馆设置)',
    floor_type VARCHAR(50) COMMENT '地面类型',
    features JSON COMMENT '特性标签',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-可用, 0-停用, 2-维护中',
    status_reason VARCHAR(255) COMMENT '状态原因',
    status_until DATETIME COMMENT '状态截止时间(临时停用)',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    INDEX idx_venue (venue_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_venue_name (venue_id, name),
    FOREIGN KEY (venue_id) REFERENCES venue(id)
) ENGINE=InnoDB COMMENT='场地表';

CREATE TABLE court_closure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    court_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL COMMENT '关闭开始时间',
    end_time DATETIME NOT NULL COMMENT '关闭结束时间',
    reason VARCHAR(255) COMMENT '关闭原因',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_court_time (court_id, start_time, end_time),
    FOREIGN KEY (court_id) REFERENCES court(id)
) ENGINE=InnoDB COMMENT='场地关闭记录表';

CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_no VARCHAR(32) NOT NULL UNIQUE COMMENT '预约单号',
    user_id BIGINT NOT NULL COMMENT '预约用户',
    user_name VARCHAR(50) COMMENT '预约时用户姓名(快照)',
    user_phone VARCHAR(20) COMMENT '预约时用户电话(快照)',
    venue_id BIGINT NOT NULL COMMENT '球馆ID',
    venue_name VARCHAR(100) COMMENT '球馆名称(快照)',
    court_id BIGINT NOT NULL COMMENT '场地ID',
    court_name VARCHAR(50) COMMENT '场地名称(快照)',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    slot_count INT NOT NULL COMMENT '占用时段数',
    booking_type TINYINT DEFAULT 1 COMMENT '预约类型: 1-个人, 2-团体',
    group_id BIGINT COMMENT '团体预约ID(团体预约时关联)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-CONFIRMED, 2-CANCELLED, 3-CHECKED_IN, 4-NO_SHOW',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    cancelled_at DATETIME COMMENT '取消时间',
    cancelled_by BIGINT COMMENT '取消人',
    cancelled_by_type TINYINT COMMENT '取消人类型: 1-用户, 2-场馆员, 3-管理员',
    checked_in_at DATETIME COMMENT '核销时间',
    checked_in_by BIGINT COMMENT '核销人',
    checkin_method TINYINT COMMENT '核销方式: 1-扫码, 2-手动',
    no_show_marked_at DATETIME COMMENT '爽约标记时间',
    no_show_marked_by BIGINT COMMENT '爽约标记人',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_venue (venue_id),
    INDEX idx_court (court_id),
    INDEX idx_date (booking_date),
    INDEX idx_status (status),
    INDEX idx_court_date (court_id, booking_date, start_time, end_time),
    INDEX idx_group (group_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (venue_id) REFERENCES venue(id),
    FOREIGN KEY (court_id) REFERENCES court(id)
) ENGINE=InnoDB COMMENT='预约表';

CREATE TABLE group_booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_no VARCHAR(32) NOT NULL UNIQUE COMMENT '团体预约单号',
    user_id BIGINT NOT NULL COMMENT '发起人',
    venue_id BIGINT NOT NULL COMMENT '球馆ID',
    booking_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    court_count INT NOT NULL COMMENT '场地数量',
    participant_count INT COMMENT '参与人数',
    participants JSON COMMENT '参与人名单',
    purpose VARCHAR(255) COMMENT '预约用途',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_venue_date (venue_id, booking_date),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (venue_id) REFERENCES venue(id)
) ENGINE=InnoDB COMMENT='团体预约表';

CREATE TABLE checkin_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    booking_no VARCHAR(32) NOT NULL,
    qr_token VARCHAR(100) COMMENT '二维码token',
    checkin_method TINYINT NOT NULL COMMENT '核销方式: 1-扫码, 2-手动',
    checked_in_by BIGINT NOT NULL COMMENT '核销人',
    checked_in_at DATETIME NOT NULL COMMENT '核销时间',
    venue_id BIGINT NOT NULL COMMENT '球馆ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking (booking_id),
    INDEX idx_qr_token (qr_token),
    INDEX idx_time (checked_in_at),
    FOREIGN KEY (booking_id) REFERENCES booking(id)
) ENGINE=InnoDB COMMENT='核销记录表';

CREATE TABLE qr_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(100) NOT NULL UNIQUE COMMENT 'token值',
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    used_at DATETIME COMMENT '使用时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_booking (booking_id),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB COMMENT='二维码token表';

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    user_type TINYINT COMMENT '用户类型: 1-普通用户, 2-场馆员, 3-管理员',
    username VARCHAR(50) COMMENT '操作用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    resource_name VARCHAR(100) COMMENT '资源名称',
    old_value JSON COMMENT '变更前值',
    new_value JSON COMMENT '变更后值',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_time (created_at)
) ENGINE=InnoDB COMMENT='审计日志表';

CREATE TABLE violation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    violation_type TINYINT NOT NULL COMMENT '违约类型: 1-爽约, 2-超时取消',
    booking_date DATE NOT NULL,
    marked_by BIGINT COMMENT '标记人',
    marked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ban_days INT COMMENT '禁用天数',
    ban_until DATETIME COMMENT '禁用截止时间',
    cleared_at DATETIME COMMENT '清除时间',
    cleared_by BIGINT COMMENT '清除人',
    INDEX idx_user (user_id),
    INDEX idx_date (booking_date),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (booking_id) REFERENCES booking(id)
) ENGINE=InnoDB COMMENT='违约记录表';

INSERT INTO role (code, name, description, is_system) VALUES
('ADMIN', '管理员', '系统管理员，拥有全部权限', 1),
('VENUE_STAFF', '场馆员', '负责球馆日常运营和核销', 1),
('USER', '普通用户', '学生/教师，可进行预约', 1);

INSERT INTO permission (code, name, resource_type, action, description) VALUES
('venue:create', '创建球馆', 'venue', 'create', '创建新球馆'),
('venue:read', '查看球馆', 'venue', 'read', '查看球馆信息'),
('venue:update', '更新球馆', 'venue', 'update', '更新球馆信息'),
('venue:delete', '删除球馆', 'venue', 'delete', '删除球馆'),
('court:create', '创建场地', 'court', 'create', '创建新场地'),
('court:read', '查看场地', 'court', 'read', '查看场地信息'),
('court:update', '更新场地', 'court', 'update', '更新场地信息'),
('court:delete', '删除场地', 'court', 'delete', '删除场地'),
('booking:create', '创建预约', 'booking', 'create', '创建预约'),
('booking:read', '查看预约', 'booking', 'read', '查看预约信息'),
('booking:cancel', '取消预约', 'booking', 'cancel', '取消预约'),
('booking:checkin', '核销签到', 'booking', 'checkin', '核销预约'),
('booking:mark_no_show', '标记爽约', 'booking', 'mark_no_show', '标记爽约'),
('user:read', '查看用户', 'user', 'read', '查看用户信息'),
('user:update', '更新用户', 'user', 'update', '更新用户信息'),
('stats:read', '查看统计', 'stats', 'read', '查看统计数据');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'ADMIN';

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM role r, permission p 
WHERE r.code = 'VENUE_STAFF' 
AND p.code IN ('venue:read', 'court:read', 'booking:read', 'booking:checkin', 'booking:mark_no_show', 'stats:read');
