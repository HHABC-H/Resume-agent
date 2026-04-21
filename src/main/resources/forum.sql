CREATE TABLE `forum_category` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL COMMENT '分类名',
  `description` VARCHAR(200) COMMENT '描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `post_count` INT DEFAULT 0 COMMENT '帖子数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_tag` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(30) NOT NULL COMMENT '标签名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_post` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `category_id` BIGINT COMMENT '分类ID',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `dislike_count` INT DEFAULT 0 COMMENT '点踩数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0正常 1精华 2置顶',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_forum_post_author` (`author_id`),
  INDEX `idx_forum_post_category` (`category_id`),
  INDEX `idx_forum_post_status` (`status`),
  INDEX `idx_forum_post_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_comment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(0或一级评论ID)',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `content` TEXT NOT NULL COMMENT '内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `dislike_count` INT DEFAULT 0 COMMENT '点踩数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_forum_comment_post` (`post_id`),
  INDEX `idx_forum_comment_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_post_tag` (
  `post_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `forum_essence` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `post_id` BIGINT NOT NULL UNIQUE COMMENT '帖子ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化分类
INSERT INTO `forum_category` (`name`, `description`, `sort_order`) VALUES
('Java', 'Java 技术交流', 1),
('Python', 'Python 技术交流', 2),
('前端', '前端技术交流', 3),
('数据库', '数据库技术交流', 4),
('求职面试', '求职面试经验分享', 5),
('职场话题', '职场经验交流', 6);
