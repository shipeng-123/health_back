CREATE TABLE `sys_user` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '账号（登录用）',
                            `phone` VARCHAR(20) NOT NULL COMMENT '手机号（登录用）',
                            `password` VARCHAR(100) NOT NULL COMMENT '密码哈希（BCrypt）',
                            `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                            `gender` TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                            `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                            `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                            `height_cm` DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
                            `goal_weight_kg` DECIMAL(5,2) DEFAULT NULL COMMENT '目标体重(kg)',
                            `activity_level` TINYINT DEFAULT NULL COMMENT '活动水平：1低 2中 3高',
                            `target_type` TINYINT DEFAULT NULL COMMENT '目标类型：1减脂 2增肌 3维持',
                            `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
                            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
                            `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_phone` (`phone`),
                            KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

ALTER TABLE sys_user
    ADD COLUMN weight DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)';
CREATE TABLE sport_record (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              sport_type VARCHAR(50) NOT NULL COMMENT '运动类型',
                              duration_min INT NOT NULL COMMENT '运动时长(分钟)',
                              distance_km DECIMAL(6,2) DEFAULT NULL COMMENT '距离(km)',
                              met_value DECIMAL(5,2) NOT NULL COMMENT 'MET值',
                              calories DECIMAL(8,2) NOT NULL COMMENT '消耗热量(kcal)',
                              record_date DATE NOT NULL COMMENT '记录日期',
                              remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (id),
                              KEY idx_user_date (user_id, record_date)
) COMMENT='运动记录表';