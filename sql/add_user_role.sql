-- =====================================================
-- 添加用户角色字段
-- =====================================================

USE `resume_agent`;

-- 为 user 表添加 role 字段
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role'
);
SET @sql = IF(@column_exists = 0,
              'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(32) NOT NULL DEFAULT ''USER'' COMMENT ''角色：ADMIN/USER'' AFTER `display_name`',
              'SELECT "role column already exists"'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 role 字段添加索引
SET @index_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND INDEX_NAME = 'idx_user_role'
);
SET @sql = IF(@index_exists = 0,
              'ALTER TABLE `user` ADD INDEX `idx_user_role` (`role`)',
              'SELECT "idx_user_role already exists"'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建默认管理员账号（如果不存在）
SET @admin_exists = (
    SELECT COUNT(*) FROM `user` WHERE `username` = 'admin'
);
SET @sql = IF(@admin_exists = 0,
              'INSERT INTO `user` (`username`, `email`, `password_hash`, `display_name`, `role`, `status`, `created_at`, `updated_at`) VALUES (''admin'', ''admin@example.com'', ''$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'', ''管理员'', ''ADMIN'', 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))',
              'SELECT "admin user already exists"'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 注意：默认管理员密码为 "admin123"

SELECT 'User role column added successfully!' AS 'Status';