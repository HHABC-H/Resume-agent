-- Resume-agent 初始化数据库脚本
-- 适用数据库：MySQL 8+

CREATE DATABASE IF NOT EXISTS `resume_agent`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `resume_agent`;

-- 1) 简历会话主表（对应 ResumeData 主体）
CREATE TABLE IF NOT EXISTS `resume_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_id` VARCHAR(64) NOT NULL COMMENT '业务简历ID(UUID)',
  `resume_text` MEDIUMTEXT NOT NULL COMMENT '简历原文',
  `status` ENUM('ANALYZED', 'QUESTIONS_READY', 'EVALUATED') NOT NULL DEFAULT 'ANALYZED' COMMENT '流程状态',

  `resume_overall_score` INT NULL COMMENT '简历总分',
  `score_project` INT NULL COMMENT '项目经验分',
  `score_skill_match` INT NULL COMMENT '技能匹配分',
  `score_content` INT NULL COMMENT '内容完整性分',
  `score_structure` INT NULL COMMENT '结构清晰度分',
  `score_expression` INT NULL COMMENT '表达专业性分',
  `resume_summary` TEXT NULL COMMENT '简历总结',

  `evaluation_session_id` VARCHAR(64) NULL COMMENT '评估会话ID',
  `evaluation_total_questions` INT NULL COMMENT '评估总题数',
  `evaluation_overall_score` INT NULL COMMENT '评估总分',
  `evaluation_overall_feedback` TEXT NULL COMMENT '评估整体反馈',

  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resume_session_resume_id` (`resume_id`),
  KEY `idx_resume_session_status` (`status`),
  KEY `idx_resume_session_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) 简历优势列表
CREATE TABLE IF NOT EXISTS `resume_strength` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `strength_text` VARCHAR(1000) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_resume_strength_session` (`resume_session_id`),
  CONSTRAINT `fk_resume_strength_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) 简历改进建议
CREATE TABLE IF NOT EXISTS `resume_suggestion` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `category` VARCHAR(120) NULL,
  `priority_level` VARCHAR(60) NULL,
  `issue_text` VARCHAR(1000) NULL,
  `recommendation` VARCHAR(2000) NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_resume_suggestion_session` (`resume_session_id`),
  CONSTRAINT `fk_resume_suggestion_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) 面试问题
CREATE TABLE IF NOT EXISTS `interview_question` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `question_text` TEXT NOT NULL,
  `question_type` VARCHAR(80) NULL,
  `category` VARCHAR(120) NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_interview_question_session` (`resume_session_id`),
  KEY `idx_interview_question_type` (`question_type`),
  CONSTRAINT `fk_interview_question_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) 评估分类得分
CREATE TABLE IF NOT EXISTS `evaluation_category_score` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `category` VARCHAR(120) NOT NULL,
  `score` INT NULL,
  `question_count` INT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_eval_category_session` (`resume_session_id`),
  CONSTRAINT `fk_eval_category_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6) 逐题评估
CREATE TABLE IF NOT EXISTS `evaluation_question_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `question_index` INT NOT NULL,
  `question_text` TEXT NOT NULL,
  `category` VARCHAR(120) NULL,
  `user_answer` MEDIUMTEXT NULL,
  `score` INT NULL,
  `feedback` TEXT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_eval_detail_session` (`resume_session_id`),
  KEY `idx_eval_detail_question_index` (`question_index`),
  CONSTRAINT `fk_eval_detail_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7) 评估优势
CREATE TABLE IF NOT EXISTS `evaluation_strength` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `strength_text` VARCHAR(1000) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_eval_strength_session` (`resume_session_id`),
  CONSTRAINT `fk_eval_strength_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8) 评估改进建议
CREATE TABLE IF NOT EXISTS `evaluation_improvement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `improvement_text` VARCHAR(1000) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_eval_improvement_session` (`resume_session_id`),
  CONSTRAINT `fk_eval_improvement_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9) 参考答案
CREATE TABLE IF NOT EXISTS `evaluation_reference_answer` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resume_session_id` BIGINT UNSIGNED NOT NULL,
  `question_index` INT NOT NULL,
  `question_text` TEXT NOT NULL,
  `reference_answer` MEDIUMTEXT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_eval_ref_answer_session` (`resume_session_id`),
  KEY `idx_eval_ref_answer_question_index` (`question_index`),
  CONSTRAINT `fk_eval_ref_answer_session`
    FOREIGN KEY (`resume_session_id`) REFERENCES `resume_session` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10) 参考答案要点
CREATE TABLE IF NOT EXISTS `evaluation_reference_key_point` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `reference_answer_id` BIGINT UNSIGNED NOT NULL,
  `key_point` VARCHAR(1000) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_eval_ref_key_point_ref` (`reference_answer_id`),
  CONSTRAINT `fk_eval_ref_key_point_ref`
    FOREIGN KEY (`reference_answer_id`) REFERENCES `evaluation_reference_answer` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11) 历史记录查询视图（首页/历史页可直接查询）
CREATE OR REPLACE VIEW `v_resume_history` AS
SELECT
  rs.`resume_id`,
  rs.`status`,
  rs.`resume_overall_score`,
  rs.`evaluation_overall_score`,
  (SELECT COUNT(*) FROM `interview_question` iq WHERE iq.`resume_session_id` = rs.`id`) AS `question_count`,
  rs.`created_at`,
  rs.`updated_at`
FROM `resume_session` rs
ORDER BY rs.`updated_at` DESC;