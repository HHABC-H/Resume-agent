/*
 * 论坛模块数据库初始化脚本
 * ================================
 * 功能：帖子发布、评论、点赞/踩、精华/置顶
 * 作者：Resume-agent Team
 * 日期：2026-04-21
 * ================================
 *
 * 表结构说明：
 *   forum_category  - 分类表（Java、Python、前端等）
 *   forum_tag       - 标签表（可自定义标签）
 *   forum_post      - 帖子表（标题、内容、作者、统计等）
 *   forum_comment   - 评论表（支持二级评论）
 *   forum_post_tag  - 帖子-标签关联表（多对多）
 *   forum_essence   - 精华帖记录表
 *
 * 状态说明（forum_post.status）：
 *   0 - 正常帖子
 *   1 - 精华帖
 *   2 - 置顶帖
 */

-- ----------------------------
-- 1. 分类表
-- 存储论坛的分类，如 Java、Python、前端等
-- ----------------------------
CREATE TABLE `forum_category` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(200) COMMENT '分类描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重（数字越小越靠前）',
  `post_count` INT DEFAULT 0 COMMENT '该分类下的帖子数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛分类表';

-- ----------------------------
-- 2. 标签表
-- 存储可选标签，如 "面试"、"Spring"、"Redis" 等
-- ----------------------------
CREATE TABLE `forum_tag` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
  `name` VARCHAR(30) NOT NULL COMMENT '标签名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛标签表';

-- ----------------------------
-- 3. 帖子表
-- 核心表，存储帖子的所有信息
-- ----------------------------
CREATE TABLE `forum_post` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
  `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '帖子正文内容',
  `author_id` BIGINT NOT NULL COMMENT '作者用户ID（关联 user 表）',
  `category_id` BIGINT COMMENT '所属分类ID（关联 forum_category 表）',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞次数',
  `dislike_count` INT DEFAULT 0 COMMENT '点踩次数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数量',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0正常 1精华 2置顶',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  -- 索引
  INDEX `idx_forum_post_author` (`author_id`) COMMENT '按作者查询',
  INDEX `idx_forum_post_category` (`category_id`) COMMENT '按分类查询',
  INDEX `idx_forum_post_status` (`status`) COMMENT '按状态查询（精华/置顶筛选）',
  INDEX `idx_forum_post_created` (`created_at`) COMMENT '按时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- ----------------------------
-- 4. 评论表
-- 支持二级评论：parent_id=0 表示一级评论，parent_id=某一级评论ID 表示二级评论（回复）
-- ----------------------------
CREATE TABLE `forum_comment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID：0表示一级评论，非0表示二级评论（回复某评论）',
  `author_id` BIGINT NOT NULL COMMENT '评论作者ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `dislike_count` INT DEFAULT 0 COMMENT '点踩数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  -- 索引
  INDEX `idx_forum_comment_post` (`post_id`) COMMENT '按帖子查询评论',
  INDEX `idx_forum_comment_parent` (`parent_id`) COMMENT '按父评论查询二级评论'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论表';

-- ----------------------------
-- 5. 帖子-标签关联表
-- 帖子和标签的多对多关系
-- ----------------------------
CREATE TABLE `forum_post_tag` (
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`post_id`, `tag_id`) COMMENT '联合主键'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子标签关联表';

-- ----------------------------
-- 6. 精华帖记录表
-- 记录哪些帖子被设为精华（与 forum_post.status=1 配合使用）
-- ----------------------------
CREATE TABLE `forum_essence` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  `post_id` BIGINT NOT NULL UNIQUE COMMENT '精华帖ID（唯一约束，防止重复设精华）',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID（设为精华的管理员）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '设为精华的时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='精华帖记录表';

-- ----------------------------
-- 7. 帖子点赞/点踩记录表
-- 记录用户对帖子的点赞/点踩状态
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_like` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1点赞 2点踩',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  INDEX `idx_forum_post_like_user` (`user_id`),
  INDEX `idx_forum_post_like_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞/点踩记录表';

-- ----------------------------
-- 8. 帖子收藏记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_bookmark` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY `uk_forum_post_bookmark_user_post` (`user_id`, `post_id`),
  INDEX `idx_forum_post_bookmark_user` (`user_id`),
  INDEX `idx_forum_post_bookmark_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏记录表';

-- ----------------------------
-- 初始化数据：默认分类
-- ----------------------------
INSERT INTO `forum_category` (`name`, `description`, `sort_order`) VALUES
('Java', 'Java 技术交流', 1),
('Python', 'Python 技术交流', 2),
('前端', '前端技术交流', 3),
('数据库', '数据库技术交流', 4),
('求职面试', '求职面试经验分享', 5),
('职场话题', '职场经验交流', 6);
