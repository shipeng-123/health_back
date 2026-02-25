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


CREATE TABLE food_item (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                           food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
                           category VARCHAR(50) DEFAULT NULL COMMENT '分类：主食/肉类/水果/饮品等',
                           calorie_per_100g DECIMAL(8,2) NOT NULL COMMENT '每100g热量(kcal)',
                           protein_per_100g DECIMAL(8,2) DEFAULT NULL COMMENT '每100g蛋白质(g)',
                           fat_per_100g DECIMAL(8,2) DEFAULT NULL COMMENT '每100g脂肪(g)',
                           carb_per_100g DECIMAL(8,2) DEFAULT NULL COMMENT '每100g碳水(g)',
                           unit_hint VARCHAR(20) DEFAULT 'g' COMMENT '默认单位提示',
                           is_builtin TINYINT NOT NULL DEFAULT 1 COMMENT '是否内置：1是 0否',
                           status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (id),
                           KEY idx_food_name (food_name),
                           KEY idx_category (category),
                           KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物库表';


CREATE TABLE diet_record (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                             user_id BIGINT NOT NULL COMMENT '用户ID',
                             food_item_id BIGINT NOT NULL COMMENT '食物ID',
                             meal_type TINYINT NOT NULL COMMENT '餐次：1早餐 2午餐 3晚餐 4加餐',
                             intake_gram DECIMAL(8,2) NOT NULL COMMENT '摄入重量(g)',
                             calorie_per_100g DECIMAL(8,2) NOT NULL COMMENT '记录时食物每100g热量快照',
                             total_calories DECIMAL(8,2) NOT NULL COMMENT '本次摄入总热量(kcal)',
                             record_date DATE NOT NULL COMMENT '记录日期',
                             remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (id),
                             KEY idx_user_date (user_id, record_date),
                             KEY idx_user_meal_date (user_id, meal_type, record_date),
                             KEY idx_food_item_id (food_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';


ALTER TABLE food_item
    ADD COLUMN user_id BIGINT NULL COMMENT '所属用户ID；NULL表示系统内置' AFTER id;

ALTER TABLE food_item
    ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用' AFTER carb_per100g;

ALTER TABLE food_item
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是' AFTER status;

ALTER TABLE food_item
    ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER deleted;

ALTER TABLE food_item
    ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;
ALTER TABLE food_item
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是' AFTER status;