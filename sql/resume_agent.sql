-- =====================================================
-- Resume Agent 数据库初始化脚本
-- 版本: 2.0
-- 包含用户角色、面试追问功能扩展
-- =====================================================

USE `resume_agent`;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `evaluation_reference_key_point`;
DROP TABLE IF EXISTS `evaluation_reference_answer`;
DROP TABLE IF EXISTS `evaluation_strength`;
DROP TABLE IF EXISTS `evaluation_question_detail`;
DROP TABLE IF EXISTS `evaluation_improvement`;
DROP TABLE IF EXISTS `evaluation_category_score`;
DROP TABLE IF EXISTS `interview_question`;
DROP TABLE IF EXISTS `resume_suggestion`;
DROP TABLE IF EXISTS `resume_strength`;
DROP TABLE IF EXISTS `resume_session`;
DROP TABLE IF EXISTS `user_session`;
DROP TABLE IF EXISTS `user`;

-- 创建用户表
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `email` VARCHAR(255) NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `display_name` VARCHAR(100) NULL COMMENT '显示名称',
  `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=激活，0=禁用',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入用户数据
INSERT INTO `user` (`id`, `username`, `email`, `password_hash`, `display_name`, `role`, `status`, `created_at`, `updated_at`) VALUES
(1, 'TTT', '3174805204@qq.com', '$2a$10$OHL8yQtFSYMfma/vIrZ5EuRE.nFIYf/yfwC1IOxU0/x14XbS0146.', 'THT', 'USER', 1, '2026-04-15 20:18:25.935', '2026-04-18 16:35:36.100'),
(2, 'admin', 'admin@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', 'ADMIN', 1, '2026-04-18 15:02:13.778', '2026-04-18 15:02:13.778');

-- =====================================================
-- 2. 用户会话表
-- =====================================================
CREATE TABLE `user_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `session_token` VARCHAR(128) NOT NULL COMMENT '会话令牌',
  `device_info` VARCHAR(255) NULL COMMENT '设备信息',
  `ip_address` VARCHAR(64) NULL COMMENT 'IP地址',
  `expires_at` DATETIME(3) NOT NULL COMMENT '过期时间',
  `revoked_at` DATETIME(3) NULL COMMENT '撤销时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_token` (`session_token`),
  KEY `idx_user_session_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 插入用户会话数据
INSERT INTO `user_session` (`id`, `user_id`, `session_token`, `device_info`, `ip_address`, `expires_at`, `revoked_at`, `created_at`, `updated_at`) VALUES
(1, 1, '9c77cfed7f2d29a6b3bc4725dfee20368e8feae9afa4be54b7d9e27f2ac949af', NULL, NULL, '2026-04-22 20:18:34.277', '2026-04-17 14:38:35.880', '2026-04-15 20:18:34.277', '2026-04-17 14:38:35.880'),
(2, 1, 'af76916747079cc921ce209dc8b1e48bf91e1e68ba8ff056feda2c45bb34a05e', NULL, NULL, '2026-04-24 14:38:40.873', NULL, '2026-04-17 14:38:40.873', '2026-04-17 14:38:40.873'),
(3, 1, '67b44e6671a2220c41ab251cf55e417e2c1692e50ada889f5db26974af317a0c', NULL, NULL, '2026-04-25 15:10:56.967', NULL, '2026-04-18 15:10:56.967', '2026-04-18 15:10:56.967'),
(4, 1, 'e409f81d34adcb6c76801b6487829d421d684565c21edfb53b8dbde33444bd2e', NULL, NULL, '2026-04-25 15:35:12.765', NULL, '2026-04-18 15:35:12.765', '2026-04-18 15:35:12.765'),
(5, 1, '6ef984fc1c7911efca2c1a4158dbdc9dfcf7feed32317747742fd7a0c0b90c77', NULL, NULL, '2026-04-25 15:40:35.050', NULL, '2026-04-18 15:40:35.050', '2026-04-18 15:40:35.050'),
(6, 1, 'a0ba2fad402d2ce69a945fbed9c21de47eb0b797da0acf597a646b6eaf7980e5', NULL, NULL, '2026-04-25 15:53:25.984', NULL, '2026-04-18 15:53:25.984', '2026-04-18 15:53:25.984'),
(7, 1, '457fff13584de2951bae19a02c62ef16a3751c29ee3135d51010c857ef3fb080', NULL, NULL, '2026-04-25 16:13:17.649', NULL, '2026-04-18 16:13:17.649', '2026-04-18 16:13:17.649'),
(8, 1, '552b8548f80eec298be6d1e713d450756f4d27f255e2d0b6a54de91370fa8d4d', NULL, NULL, '2026-04-25 16:13:34.313', NULL, '2026-04-18 16:13:34.313', '2026-04-18 16:13:34.313'),
(9, 1, 'a27bbe7e6c18e5314854723127bc74bc2276bb6021eb1d51f5424025dabf5fba', NULL, NULL, '2026-04-25 16:23:29.657', NULL, '2026-04-18 16:23:29.657', '2026-04-18 16:23:29.657'),
(10, 1, 'ef373cec53003e799eb0e4e53b5122cc067d91644677ce4aabe91e90da8b3ac3', NULL, NULL, '2026-04-25 16:33:11.896', NULL, '2026-04-18 16:33:11.896', '2026-04-18 16:33:11.896'),
(11, 1, '4ba3f25e60a6a587c5d35e1bfa472eb5356f0fc6953d5c0b97e820ef2082c8d6', NULL, NULL, '2026-04-25 16:38:06.671', NULL, '2026-04-18 16:38:06.671', '2026-04-18 16:38:06.671'),
(12, 1, '0e258ad2ca0a065c93e98d49b94cbbb8bc0202882b516a37aaad1632952886ee', NULL, NULL, '2026-04-25 16:58:08.297', NULL, '2026-04-18 16:58:08.297', '2026-04-18 16:58:08.297'),
(13, 1, 'c95530c85f4ea702469a32ec297f5346484bcee0ec3bfdf019eb9a553d388c86', NULL, NULL, '2026-04-25 21:50:48.115', NULL, '2026-04-18 21:50:48.115', '2026-04-18 21:50:48.115'),
(14, 1, '24fcb037a9c15d28b358e25bd7a2924acba1db80f8892a796f60ce186a7d4fa1', NULL, NULL, '2026-04-26 14:17:51.187', NULL, '2026-04-19 14:17:51.187', '2026-04-19 14:17:51.187');

-- =====================================================
-- 3. 简历会话主表
-- =====================================================
CREATE TABLE `resume_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NULL COMMENT '用户ID',
  `resume_id` VARCHAR(64) NOT NULL COMMENT '简历ID',
  `resume_text` MEDIUMTEXT NOT NULL COMMENT '简历文本内容',
  `status` ENUM('ANALYZED', 'QUESTIONS_READY', 'EVALUATED') NOT NULL DEFAULT 'ANALYZED' COMMENT '状态',
  `position_type` VARCHAR(32) NOT NULL DEFAULT 'BACKEND_JAVA' COMMENT '职位类型：BACKEND_JAVA/FRONTEND/ALGORITHM',
  `resume_overall_score` INT NULL COMMENT '简历综合评分',
  `score_project` INT NULL COMMENT '项目评分',
  `score_skill_match` INT NULL COMMENT '技能匹配评分',
  `score_content` INT NULL COMMENT '内容评分',
  `score_structure` INT NULL COMMENT '结构评分',
  `score_expression` INT NULL COMMENT '表达评分',
  `resume_summary` TEXT NULL COMMENT '简历摘要',
  `evaluation_session_id` VARCHAR(64) NULL COMMENT '面试评估会话ID',
  `evaluation_total_questions` INT NULL COMMENT '面试问题数量',
  `evaluation_overall_score` INT NULL COMMENT '面试综合评分',
  `evaluation_overall_feedback` TEXT NULL COMMENT '面试综合反馈',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resume_id` (`resume_id`),
  KEY `idx_resume_session_user_id` (`user_id`),
  KEY `idx_resume_session_status` (`status`),
  KEY `idx_resume_session_position_type` (`position_type`),
  KEY `idx_resume_session_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历会话主表';

-- 插入简历会话数据
INSERT INTO `resume_session` (`id`, `user_id`, `resume_id`, `resume_text`, `status`, `position_type`, `resume_overall_score`, `score_project`, `score_skill_match`, `score_content`, `score_structure`, `score_expression`, `resume_summary`, `evaluation_session_id`, `evaluation_total_questions`, `evaluation_overall_score`, `evaluation_overall_feedback`, `created_at`, `updated_at`) VALUES
(1, 1, '2c01b076-a378-465f-bc66-fac6fc99a268', '简历内容1...', 'QUESTIONS_READY', 'BACKEND_JAVA', 42, 12, 8, 9, 3, 10, '简历摘要1...', NULL, NULL, NULL, NULL, '2026-04-15 20:36:32.557', '2026-04-15 23:59:40.034'),
(2, 1, 'e7c30c34-e021-463d-9c7e-73627a74e8b7', '简历内容2...', 'QUESTIONS_READY', 'BACKEND_JAVA', 87, 38, 19, 14, 5, 11, '简历摘要2...', NULL, NULL, NULL, NULL, '2026-04-15 23:36:02.940', '2026-04-17 16:49:06.426'),
(3, 1, '5a1d37f3-b117-43df-ab78-53d9feb9eb14', '黄 启 华\n求职意向：Java后端开发实习 广州\n年龄： 20岁 性别： 男\n政治面貌： 共青团员 电话： 13538528694 邮箱： 13538528694@163.com\n教育背景\n2023-09 ~ 至今 仲恺农业工程学院 计算机科学与技术（本科）专业成绩：专业前10%\n主修课程：Java程序设计、数据结构与算法、数据库系统原理、Java Web开发、操作系统、计算机网络、python程序设计、Linux 系统与应用等\n项目经验\n2025-01 ~ 2025-02 智能餐饮AI一体化平台 后端java开发\n技术栈：Spring Boot、MyBatis、MySQL、Redis、JWT、WebSocket、Docker、Spring AI、DeepSeek API\n项目简介：为连锁餐饮企业设计的线上外卖与门店管理一体化系统，覆盖用户端点餐、商户端接单、管理后台监控全流程，支持商品浏览、购物车、订单管理、秒杀活动等核心业务。创新性引入Spring AI大模型智能客服，实现7x24小时自动咨询。负责后端核心模块开发，在高并发订单处理、AI对话持久化、缓存加速等方面完成关键技术落地。\n核心实现：\n· 高性能认证拦截：基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题\n· 缓存加速：针对高频访问的菜品及套餐数据，设计Redis缓存策略，接口响应速度提升约30%\n· 可靠订单闭环：利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消；通过WebSocket推送实时订单状态\n· 数据可视化：基于MySQL聚合查询和ECharts，开发营业数据报表模块，提供订单趋势、销售额统计等可视化展示\n· 智能客服与对话记忆：基于Spring AI接入大模型API，封装智能客服接口处理咨询，日均处理约200条；设计Message-Thread模型将对话持久化至MySQL；结合ChatMemory与Thread ID实现上下文记忆；开发历史会话加载功能，支持跨会话"断点续聊"。\n\n2025-03 ~ 2025-04 微服务分布式电商系统 后端java开发\n技术栈：Spring Cloud、Nacos、MyBatis-Plus、Gateway、Sentinel、Seata、RabbitMQ、Elasticsearch、Docker\n项目简介：基于微服务架构的B2C商城系统，将传统单体拆分为用户、商品、订单、库存、搜索等独立服务。负责核心业务模块开发，解决了分布式事务、服务容错及高性能搜索等关键问题，提升系统可扩展性与高可用能力。\n核心实现：\n· 服务治理：使用Nacos作为注册与配置中心，实现服务动态发现及配置热更新，支持横向扩容。\n· 统一网关：基于Spring Cloud Gateway构建API网关，统一处理路由转发、跨域、全局日志及Sentinel流量控制。\n· 服务容错：采用Feign + Sentinel实现服务间调用熔断降级，配置QPS限流规则，避免单点故障引发服务雪崩。\n· 分布式事务：引入Seata AT模式解决下单跨服务（订单、库存、积分）数据一致性问题，保障业务原子性。\n· 异步解耦：订单创建成功后通过RabbitMQ异步通知库存扣减、积分增加，核心接口响应时间缩短约40%。\n· 搜索引擎：集成Elasticsearch替代MySQL模糊查询，实现商品多维度（价格、销量、规格）分词搜索，查询性能提升显著。\n个人技能\n· 熟悉Java基础，熟悉常用集合框架、反射、异常等，了解JUC、锁机制、线程池、ThreadLocal等\n· 熟悉 MySQL，了解事务隔离级别、存储引擎、索引、锁机制、MVCC、各种日志等\n· 了解Redis基本数据结构、持久化机制、分布式锁、内存淘汰策略等，了解缓存击穿、穿透、雪崩的解决方案\n· 熟悉SpringBoot,MyBatis等框架，了解IoC、AOP、Bean生命周期等\n· 了解JVM内存结构、垃圾回收机制、类加载机制、双亲委派等\n· 了解消息队列RabbitMQ的使用，了解消息重复消费等常见问题的解决方案\n· 熟悉git版本管理、Docker常用命令及Linux常用操作\n· 善用AI编程工具(claude code、codex)提升开发效率', 'ANALYZED', 'BACKEND_JAVA', 86, 38, 19, 13, 5, 10, '简历整体质量优秀，具备扎实的Java工程能力与前沿技术敏感度（Spring AI、DeepSeek、Seata、Elasticsearch等），项目选型合理、业务闭环清晰，量化意识强；唯一短板是技能描述偏浅层、部分技术点未展开深度，且自我介绍缺失（默认扣分项已规避）。', NULL, NULL, NULL, NULL, '2026-04-18 16:14:12.087', '2026-04-18 16:14:12.087'),
(4, 1, '703e4eea-ff64-41a4-9bc1-c84d20ca3b70', '张三\n\n求职意向：Java 后端开发工程师\n\n个人信息\n- 手机：13800138000\n- 邮箱：zhangsan@email.com\n- 学历：本科 - 计算机科学与技术\n\n教育经历\n2019.09 - 2023.06 XX 大学 计算机科学与技术 本科\n\n专业技能\n- 熟悉 Java 编程语言，了解 JVM 原理\n- 熟悉 Spring、Spring Boot、Spring Cloud 等框架\n- 熟悉 MySQL 数据库，了解索引优化\n- 熟悉 Redis 缓存技术\n- 了解分布式系统和微服务架构\n\n项目经历\n1. 电商系统（2022.03 - 2022.06）\n  技术栈：Spring Boot + MySQL + Redis\n  - 负责用户模块的开发\n  - 实现了登录注册功能\n  - 使用 Redis 做缓存\n\n2. 图书管理系统（2021.09 - 2021.12）\n  技术栈：Spring Boot + Vue + MySQL\n  - 参与了后端 API 开发\n  - 实现了图书的增删改查\n  - 使用了 MyBatis 作为 ORM 框架\n\n校园经历\n- 担任班级学习委员\n- 参加过程序设计竞赛\n\n自我评价\n- 学习能力强，对新技术充满热情\n- 有良好的团队合作精神\n- 能吃苦耐劳', 'QUESTIONS_READY', 'BACKEND_JAVA', 42, 12, 8, 9, 3, 0, '简历为应届本科生基础技术岗求职材料，项目均为教学级CRUD系统，缺乏真实业务场景、技术深度与量化结果；技能描述空泛无分层，未体现任何高并发、分布式、性能调优等核心能力；表达严重口语化、冗余且无STAR结构；整体技术竞争力远低于中高级Java后端岗位基准线。', NULL, NULL, NULL, NULL, '2026-04-18 16:25:10.438', '2026-04-18 21:51:22.128'),
(5, 1, 'ea98c31b-73f6-412f-beb2-9db957586c86', '黄 启 华1\n\n求职意向：Java后端开发实习 广州\n\n年龄： 20岁 性别： 男\n邮箱： 13538528694@163.com 电话： 13538528694\n\n2023-09 ~ 2027.06 仲恺农业工程学院 计算机科学与技术\n专业成绩：专业前10%\n主修课程：Java程序设计、数据结构与算法、数据库系统原理、Java Web开发、操作系统、计算机网络、python程序设计、Linux 系统与应用等\n\n智能餐饮AI一体化平台 后端java开发\n技术栈：Spring Boot、MyBatis、MySQL、Redis、JWT、WebSocket、Spring AI\n项目简介：为连锁餐饮企业设计的线上外卖与门店管理一体化系统，覆盖用户端点餐、商户端接单、管理后台监控全流程，支持商品浏览、购物车、订单管理、秒杀活动等核心业务。创新性引入Spring AI大模型智能客服，实现7x24小时自动咨询。负责后端核心模块开发，在高并发订单处理、AI对话持久化、缓存加速等方面完成关键技术落地。\n核心实现：\n· 高性能认证拦截：基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题；设计Token自动续期机制，提升用户体验\n· 缓存加速：针对高频访问的菜品及套餐数据，设计Redis缓存策略，接口响应速度提升约30%；通过设置随机过期时间避免缓存雪崩，使用布隆过滤器解决缓存穿透问题\n· 数据库优化：为订单表、菜品表建立联合索引，优化慢查询SQL，接口响应时间降低25%；使用MyBatis批量操作提升数据写入效率\n· 可靠订单闭环：利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消；通过WebSocket推送实时订单状态；设计订单状态机，规范订单流转逻辑，避免状态异常\n· 数据可视化：基于MySQL聚合查询和ECharts，开发营业数据报表模块，提供订单趋势、销售额统计等可视化展示；通过定时任务预聚合报表数据，减少实时查询压力\n· 智能客服与对话记忆：基于Spring AI接入大模型API，封装智能客服接口处理咨询，日均处理约200条；设计Message-Thread模型将对话持久化至MySQL；结合ChatMemory与Thread ID实现上下文记忆；开发历史会话加载功能，支持跨会话"断点续聊"；接入敏感词过滤机制，保障对话内容安全\n· 系统安全与日志：实现全局异常处理器，统一异常返回格式；使用AOP记录关键操作日志，便于问题追踪；对用户密码、手机号等敏感信息进行MD5+盐加密存储\n\n微服务分布式电商系统 后端java开发\n技术栈：Spring Cloud、Nacos、MyBatis-Plus、Gateway、Sentinel、Seata、RabbitMQ、Elasticsearch、Docker\n项目简介：基于微服务架构的B2C商城系统，将传统单体拆分为用户、商品、订单、库存、搜索等独立服务。负责核心业务模块开发，解决了分布式事务、服务容错及高性能搜索等关键问题，提升系统可扩展性与高可用能力。\n核心实现：\n· 服务治理：使用Nacos作为注册与配置中心，实现服务动态发现及配置热更新，支持横向扩容；配置多环境（dev/prod）命名空间隔离，避免环境冲突\n· 统一网关：基于Spring Cloud Gateway构建API网关，统一处理路由转发、跨域、全局日志及Sentinel流量控制；实现网关级限流，配置IP黑白名单，防止恶意请求\n· 服务容错：采用Feign + Sentinel实现服务间调用熔断降级，配置QPS限流规则，避免单点故障引发服务雪崩；设计降级回调逻辑，返回友好提示或兜底数据\n· 分布式事务：引入Seata AT模式解决下单跨服务（订单、库存、积分）数据一致性问题，保障业务原子性；针对高并发场景，优化事务分组，减少全局锁冲突\n· 异步解耦：订单创建成功后通过RabbitMQ异步通知库存扣减、积分增加，核心接口响应时间缩短约40%；配置消息确认机制+手动ACK，保证消息不丢失；设计死信队列处理异常消息\n· 搜索引擎：集成Elasticsearch替代MySQL模糊查询，实现商品多维度（价格、销量、规格）分词搜索，查询性能提升显著；设计ES与MySQL双写一致性方案，使用MQ同步增量数据\n· 数据库优化：分库分表设计，订单表按用户ID哈希分片，缓解单表数据压力；使用MyBatis-Plus乐观锁解决库存超卖问题\n· 监控与运维：集成Spring Boot Admin监控微服务健康状态；使用Docker Compose编排容器，一键部署整套环境；编写Shell脚本实现自动化打包部署\n· Java基础：熟悉集合框架、反射、异常、JUC并发编程（锁、线程池、ThreadLocal）\n· 数据库：熟悉MySQL（事务隔离级别、索引优化、MVCC、锁机制、SQL调优）；熟悉Redis（数据结构、持久化、分布式锁、缓存雪崩/穿透/击穿解决方案）\n· 主流框架：熟练掌握Spring Boot、MyBatis，了解IoC/AOP及Bean生命周期；熟悉Spring Cloud微服务生态（Nacos、Gateway、Sentinel、Seata）\n· 中间件与工具：熟悉RabbitMQ消息确认及重复消费问题；熟悉Elasticsearch分词搜索原理；熟悉Git、Docker、Linux常用命令\n· 开发效率：善用AI编程工具（Claude Code、Codex）提升开发效率；具备良好的代码规范和文档编写习惯\n\n个人技能\n2023-09 ~ 2027.06 仲恺农业工程学院 计算机科学与技术 专业成绩：专业前10%\n智能餐饮AI一体化平台 后端java开发\n微服务分布式电商系统 后端java开发', 'ANALYZED', 'BACKEND_JAVA', 87, 38, 19, 14, 5, 11, '这是一份极具潜力的应届生技术简历：项目选型前沿（Spring AI、Seata、ES双写）、技术覆盖全面（高并发/分布式/缓存/安全/AI集成），业务闭环清晰，量化意识强；但存在结构冗余、技能颗粒度不足、部分技术表述未达工业级深度等问题。', NULL, NULL, NULL, NULL, '2026-04-18 16:52:07.440', '2026-04-18 16:52:07.440'),
(6, 1, 'b2ffbc83-f3f5-46c8-a257-41ec53b3c4da', '黄 启 华1\n\n求职意向：Java后端开发实习 广州\n\n年龄： 20岁 性别： 男\n邮箱： 13538528694@163.com 电话： 13538528694\n\n2023-09 ~ 2027.06 仲恺农业工程学院 计算机科学与技术\n专业成绩：专业前10%\n主修课程：Java程序设计、数据结构与算法、数据库系统原理、Java Web开发、操作系统、计算机网络、python程序设计、Linux 系统与应用等\n\n智能餐饮AI一体化平台 后端java开发\n技术栈：Spring Boot、MyBatis、MySQL、Redis、JWT、WebSocket、Spring AI\n项目简介：为连锁餐饮企业设计的线上外卖与门店管理一体化系统，覆盖用户端点餐、商户端接单、管理后台监控全流程，支持商品浏览、购物车、订单管理、秒杀活动等核心业务。创新性引入Spring AI大模型智能客服，实现7x24小时自动咨询。负责后端核心模块开发，在高并发订单处理、AI对话持久化、缓存加速等方面完成关键技术落地。\n核心实现：\n· 高性能认证拦截：基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题；设计Token自动续期机制，提升用户体验\n· 缓存加速：针对高频访问的菜品及套餐数据，设计Redis缓存策略，接口响应速度提升约30%；通过设置随机过期时间避免缓存雪崩，使用布隆过滤器解决缓存穿透问题\n· 数据库优化：为订单表、菜品表建立联合索引，优化慢查询SQL，接口响应时间降低25%；使用MyBatis批量操作提升数据写入效率\n· 可靠订单闭环：利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消；通过WebSocket推送实时订单状态；设计订单状态机，规范订单流转逻辑，避免状态异常\n· 数据可视化：基于MySQL聚合查询和ECharts，开发营业数据报表模块，提供订单趋势、销售额统计等可视化展示；通过定时任务预聚合报表数据，减少实时查询压力\n· 智能客服与对话记忆：基于Spring AI接入大模型API，封装智能客服接口处理咨询，日均处理约200条；设计Message-Thread模型将对话持久化至MySQL；结合ChatMemory与Thread ID实现上下文记忆；开发历史会话加载功能，支持跨会话"断点续聊"；接入敏感词过滤机制，保障对话内容安全\n· 系统安全与日志：实现全局异常处理器，统一异常返回格式；使用AOP记录关键操作日志，便于问题追踪；对用户密码、手机号等敏感信息进行MD5+盐加密存储\n\n微服务分布式电商系统 后端java开发\n技术栈：Spring Cloud、Nacos、MyBatis-Plus、Gateway、Sentinel、Seata、RabbitMQ、Elasticsearch、Docker\n项目简介：基于微服务架构的B2C商城系统，将传统单体拆分为用户、商品、订单、库存、搜索等独立服务。负责核心业务模块开发，解决了分布式事务、服务容错及高性能搜索等关键问题，提升系统可扩展性与高可用能力。\n核心实现：\n· 服务治理：使用Nacos作为注册与配置中心，实现服务动态发现及配置热更新，支持横向扩容；配置多环境（dev/prod）命名空间隔离，避免环境冲突\n· 统一网关：基于Spring Cloud Gateway构建API网关，统一处理路由转发、跨域、全局日志及Sentinel流量控制；实现网关级限流，配置IP黑白名单，防止恶意请求\n· 服务容错：采用Feign + Sentinel实现服务间调用熔断降级，配置QPS限流规则，避免单点故障引发服务雪崩；设计降级回调逻辑，返回友好提示或兜底数据\n· 分布式事务：引入Seata AT模式解决下单跨服务（订单、库存、积分）数据一致性问题，保障业务原子性；针对高并发场景，优化事务分组，减少全局锁冲突\n· 异步解耦：订单创建成功后通过RabbitMQ异步通知库存扣减、积分增加，核心接口响应时间缩短约40%；配置消息确认机制+手动ACK，保证消息不丢失；设计死信队列处理异常消息\n· 搜索引擎：集成Elasticsearch替代MySQL模糊查询，实现商品多维度（价格、销量、规格）分词搜索，查询性能提升显著；设计ES与MySQL双写一致性方案，使用MQ同步增量数据\n· 数据库优化：分库分表设计，订单表按用户ID哈希分片，缓解单表数据压力；使用MyBatis-Plus乐观锁解决库存超卖问题\n· 监控与运维：集成Spring Boot Admin监控微服务健康状态；使用Docker Compose编排容器，一键部署整套环境；编写Shell脚本实现自动化打包部署\n· Java基础：熟悉集合框架、反射、异常、JUC并发编程（锁、线程池、ThreadLocal）\n· 数据库：熟悉MySQL（事务隔离级别、索引优化、MVCC、锁机制、SQL调优）；熟悉Redis（数据结构、持久化、分布式锁、缓存雪崩/穿透/击穿解决方案）\n· 主流框架：熟练掌握Spring Boot、MyBatis，了解IoC/AOP及Bean生命周期；熟悉Spring Cloud微服务生态（Nacos、Gateway、Sentinel、Seata）\n· 中间件与工具：熟悉RabbitMQ消息确认及重复消费问题；熟悉Elasticsearch分词搜索原理；熟悉Git、Docker、Linux常用命令\n· 开发效率：善用AI编程工具（Claude Code、Codex）提升开发效率；具备良好的代码规范和文档编写习惯\n\n个人技能\n2023-09 ~ 2027.06 仲恺农业工程学院 计算机科学与技术 专业成绩：专业前10%\n智能餐饮AI一体化平台 后端java开发\n微服务分布式电商系统 后端java开发', 'QUESTIONS_READY', 'FRONTEND', 86, 38, 19, 14, 5, 10, '简历整体质量优秀，具备扎实的Java后端技术栈、真实高价值项目实践（含AI集成与微服务落地），业务闭环清晰、量化意识强；结构略显松散，部分技术表述可进一步精炼并强化深度归因。', NULL, NULL, NULL, NULL, '2026-04-19 14:18:34.373', '2026-04-19 14:21:26.930');

-- =====================================================
-- 4. 简历优势表
-- =====================================================
CREATE TABLE `resume_strength` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '优势ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `strength_text` VARCHAR(1000) NOT NULL COMMENT '优势文本',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_resume_strength_session_id` (`resume_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历优势表';

-- 插入简历优势数据
INSERT INTO `resume_strength` (`id`, `resume_session_id`, `strength_text`, `sort_order`) VALUES
(1, 1, '项目选型前沿', 0),
(2, 1, '技术栈全面', 1),
(3, 2, '项目真实且有深度', 0),
(4, 2, '量化意识强', 1),
(5, 2, '技术敏感度高', 2),
(6, 3, '项目选型前沿（Spring AI、Seata、ES双写）', 0),
(7, 3, '技术覆盖全面（高并发/分布式/缓存/安全/AI集成）', 1),
(8, 3, '业务闭环清晰', 2),
(9, 3, '量化意识强', 3),
(10, 4, '基础技术知识掌握', 0),
(11, 5, '项目选型前沿（Spring AI、Seata、ES双写）', 0),
(12, 5, '技术覆盖全面（高并发/分布式/缓存/安全/AI集成）', 1),
(13, 5, '业务闭环清晰', 2),
(14, 5, '量化意识强', 3),
(15, 5, 'AI工程化能力突出', 4),
(16, 6, '项目选型前沿（Spring AI、Seata、ES双写）', 0),
(17, 6, '技术覆盖全面（高并发/分布式/缓存/安全/AI集成）', 1),
(18, 6, '业务闭环清晰', 2),
(19, 6, '量化意识强', 3),
(20, 6, '具备微服务完整落地经验', 4),
(21, 6, 'AI集成能力强', 5);

-- =====================================================
-- 5. 简历建议表
-- =====================================================
CREATE TABLE `resume_suggestion` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '建议ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `category` VARCHAR(120) NULL COMMENT '类别',
  `priority_level` VARCHAR(60) NULL COMMENT '优先级',
  `issue_text` VARCHAR(1000) NULL COMMENT '问题文本',
  `recommendation` VARCHAR(2000) NULL COMMENT '建议',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_resume_suggestion_session_id` (`resume_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历建议表';

-- 插入简历建议数据
INSERT INTO `resume_suggestion` (`id`, `resume_session_id`, `category`, `priority_level`, `issue_text`, `recommendation`, `sort_order`) VALUES
(1, 1, '项目', '高', '项目描述缺乏STAR结构，无量化结果', '按STAR重写——例：''主导设计基于Redis Lua原子脚本+本地Caffeine缓存的两级库存扣减方案（S），应对618大促期间瞬时3w+ QPS抢购请求（T），解决传统DB扣减导致的超卖与数据库连接池耗尽问题（A），最终实现库存一致性100%、下单RT稳定在120ms内（R）。', 0),
(2, 1, '技能', '高', '技能描述全为''熟悉/了解''级模糊表述，无技术纵深', '按【技术优化基准】展开每个技术点：例将''熟悉 Redis 缓存技术''改为''熟练应用 Redis 解决高并发场景典型问题：① 基于Redisson MultiLock实现跨服务订单状态更新分布式锁；② 设计布隆过滤器+空值缓存组合方案拦截99.2%缓存穿透请求；③ 通过随机过期时间+永不过期+主动更新机制规避缓存雪崩''。技能点扩充至8+，禁用''了解''，统一用''熟练应用'' ''主导落地'' ''深度排查''等强动词。', 1),
(3, 1, '表达', '高', '全文无一句动词开头的技术动作描述', '所有项目条目强制以强动词开头并绑定技术动作+业务目标：原句''使用 Redis 做缓存''→优化为''引入Redis Cluster集群替代单点缓存，设计读写分离+Cache-Aside模式，支撑商品详情页QPS从800提升至12,000，缓存命中率98.7%''', 2),
(4, 1, '内容', '中', '缺失关键模块：无求职意向具体岗位JD匹配说明；无工作/实习经历', '增加''技术实践''模块：补充课程设计中用ShardingSphere实现分库分表的图书借阅统计模块；或GitHub上维护的Spring Boot Starter开源小工具', 3),
(5, 1, '格式', '低', '技术名词大小写不规范', '全局执行名词标准化：Java, Spring Boot, MySQL, Redis, MyBatis, Vue, GitHub, JVM, QPS, RT', 4),
(6, 2, '项目', '高', ''高性能认证拦截''描述停留在ThreadLocal+JWT续期', '原句：''基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题；设计Token自动续期机制，提升用户体验'' → 优化句：''主导JWT无状态鉴权体系重构：采用RSA256非对称签名替代HMAC防止密钥泄露；通过Redis ZSet维护Token黑名单（按过期时间排序），支持10w+并发登录态实时吊销；引入Token自动续期+滑动窗口机制，使用户平均会话时长延长至4h，登录相关接口P95 RT从85ms降至22ms''', 0),
(7, 2, '项目', '高', ''可靠订单闭环''中Redis分布式锁未说明具体实现方式', '原句：''利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消'' → 优化句：''基于Redisson RLock实现幂等订单取消：采用看门狗自动续期+3次指数退避重试，锁竞争失败率<0.03%；结合Spring State Machine建模6阶订单状态流转，通过事件驱动+数据库版本号校验保障状态跃迁原子性，上线后订单状态错乱归零''', 1),
(8, 2, '项目', '中', ''智能客服与对话记忆''中日均200条咨询量偏低', '原句：''基于Spring AI接入大模型API，封装智能客服接口处理咨询，日均处理约200条'' → 优化句：''构建轻量级AI客服中台：基于Spring AI + Ollama本地部署Phi-3模型实现流式响应，Prompt压缩+历史Token动态截断将单次推理Token消耗降低62%；集成FAQ知识库RAG增强，Top3回答准确率提升至89%''', 2),
(9, 2, '技能', '高', '技能栏未体现JVM调优、线程池治理、GC分析等高价值点', '在''Java基础''条目下补充：''深入JVM：通过Arthas诊断Full GC频发问题，调整G1RegionSize+MaxGCPauseMillis参数，使订单服务Young GC频率下降70%''', 3),
(10, 2, '内容', '中', '缺少显性自我介绍模块', '在简历顶部新增【个人定位】模块（2行内）：''专注AI与高并发融合场景的Java后端工程师；具备从Spring AI集成、Redis多级缓存到Seata分布式事务的全链路落地能力''', 4),
(11, 3, '技能', '高', '技能栏仅罗列''了解/熟悉''，未体现深度掌握', '将''了解''升级为''掌握''并补充场景：例如''掌握Redis缓存三高问题应对策略——布隆过滤器防穿透、空值缓存+逻辑过期防击穿、多级缓存+熔断降级防雪崩''', 0),
(12, 3, '项目', '高', ''高性能认证拦截''中ThreadLocal存在技术风险', '原句：''基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题'' → 优化为：''基于JWT实现无状态鉴权，采用TransmittableThreadLocal（TTL）透传用户上下文，兼容Feign远程调用、RabbitMQ异步消费等跨线程场景''', 1),
(13, 3, '项目', '中', ''可靠订单闭环''中Spring Task为单机定时任务', '原句：''利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消'' → 优化为：''基于XXL-JOB分布式任务调度平台，按订单ID分片触发超时关单；使用Redisson RLock实现可重入、自动续期的订单级分布式锁''', 2),
(14, 3, '内容', '中', '缺少标准''自我介绍''模块', '新增''个人简介''板块（置于教育背景前），用3行凝练表达：例如''Java后端开发者，专注高并发系统设计与AI工程化落地。主导构建支持千级并发的AI客服系统，实现对话上下文跨会话持久化''', 3),
(15, 3, '格式', '低', '技术名词大小写基本规范，但''Spring AI''在项目中写作''Spring AI''（正确），但技能栏未提及', '在''个人技能''末尾增加一行：''AI工程化：掌握Spring AI框架集成范式，熟练对接DeepSeek、Qwen等大模型API，实现Prompt编排、ChatMemory管理、流式响应封装''', 4),
(16, 4, '项目', '高', '两个项目均为典型教学CRUD系统，无真实业务复杂度', '立即替换为1个深度优化的真实项目：例如将''电商系统''升级为''高并发秒杀子系统''，明确写出''主导基于Redisson分布式锁+库存预扣减+Lua原子脚本防超卖，支撑5000+并发下单，超卖率从3.2%降至0%''', 0),
(17, 4, '技能', '高', '技能点仅6项，全部使用''熟悉/了解''模糊表述', '重写专业技能模块，按''技术领域-掌握程度-具体能力点''展开。示例：''Redis：熟练掌握，包括缓存穿透（布隆过滤器+空值缓存）、击穿（Redisson读写锁）、雪崩（随机过期+多级缓存）、分布式锁（RedLock原理与Redisson实践）''', 1),
(18, 4, '表达', '高', '项目描述全部为被动句式，无动词主导', '项目经历全部改写为动词开头的STAR短句。原句''实现了登录注册功能''→优化为''主导JWT+Redis Token黑名单实现无状态登录鉴权，支持单机5k QPS''', 2),
(19, 4, '内容', '中', '缺失求职意向与教育经历的强关联说明', '在教育经历末尾补充1行技术锚点：''主修课程：数据结构与算法（94分）、分布式系统（课程设计：基于Raft实现简易KV存储）''', 3),
(20, 4, '格式', '低', '技术名词大小写不规范（如''spring boot''应为''Spring Boot''）', '全局替换技术名词为标准大小写（Spring Boot, MySQL, Redis, Vue, MyBatis, JVM）；将''项目经历''标题改为''项目实践''，并在其前增加''实习经历''模块', 4),
(21, 5, '项目', '高', ''高性能认证拦截''中ThreadLocal表述不严谨', '原句：''基于JWT实现无状态登录，结合ThreadLocal解决线程内用户信息共享问题；设计Token自动续期机制'' → 优化为：''基于JWT+Redis Token黑名单实现无状态鉴权，通过自定义RequestInterceptor在Feign调用透传用户上下文，替代ThreadLocal以支持异步/远程场景；设计滑动窗口式Token自动续期（有效期2h，活跃用户每30min刷新）''', 0),
(22, 5, '项目', '高', ''缓存加速''未体现多级缓存架构', '原句：''针对高频访问的菜品及套餐数据，设计Redis缓存策略，接口响应速度提升约30%'' → 优化为：''构建Caffeine（本地）+ Redis（分布式）两级缓存，热点菜品查询命中率提升至99.2%''', 1),
(23, 5, '项目', '中', ''可靠订单闭环''中Spring Task在分布式环境下存在单点失效风险', '原句：''利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消'' → 优化为：''基于ShedLock+Redis实现分布式定时任务调度，保障订单超时关闭、自动确认等核心任务在集群中仅执行一次''', 2),
(24, 5, '技能', '高', '技能栏未区分掌握程度', '将''熟悉Redis''重构为：''深入掌握Redis核心机制：① 缓存治理——随机TTL防雪崩、布隆过滤器+空对象缓存防穿透；② 分布式锁——基于Redisson RLock实现可重入、看门狗续期；③ 数据一致性——Cache-Aside模式+延时双删+Binlog监听补偿''', 3),
(25, 5, '内容', '中', '简历结构存在严重冗余', '删除所有重复教育信息，仅在''教育背景''模块保留一行标准格式：''2023.09 – 2027.06 仲恺农业工程学院 | 计算机科学与技术（本科）| GPA 3.7/4.0（专业前10%）''', 4),
(26, 5, '表达', '低', '技术名词大小写不规范', '全文扫描并修正：''mysql''→''MySQL''、''redis''→''Redis''、''docker''→''Docker''、''elasticsearch''→''Elasticsearch''、''rabbitmq''→''RabbitMQ''', 5),
(27, 6, '项目', '高', ''智能餐饮AI一体化平台''中''缓存加速''条目未体现多级缓存架构', '原句：''针对高频访问的菜品及套餐数据，设计Redis缓存策略，接口响应速度提升约30%'' → 优化为：''构建Redis+Caffeine两级缓存：Caffeine缓存热点菜品ID（TTL=10s），Redis存储完整详情（TTL随机化+布隆过滤器兜底），解决缓存击穿与网络抖动影响，核心查询P99 RT从1.1s降至0.78s''', 0),
(28, 6, '项目', '高', ''微服务电商系统''中''异步解耦''未体现编排逻辑复杂性', '原句：''订单创建成功后通过RabbitMQ异步通知库存扣减、积分增加'' → 优化为：''基于RabbitMQ延时队列+死信机制实现订单超时自动关闭；采用Saga模式拆分下单流程：本地事务落单→发MQ扣库存→MQ消费成功后发积分事件''', 1),
(29, 6, '项目', '中', ''可靠订单闭环''中Spring Task定时任务在分布式环境下存在单点失效风险', '原句：''利用Spring Task实现定时任务，结合Redis分布式锁防止重复取消'' → 优化为：''替换Spring Task为XXL-JOB分布式调度中心，配置订单超时检测任务（每30s触发），通过Redis分布式锁+任务分片保证多节点不重复执行''', 2),
(30, 6, '技能', '中', '技能栏中''熟悉Redis''未展开击穿/穿透/雪崩的具体应对方案', '将''熟悉Redis（数据结构、持久化、分布式锁、缓存雪崩/穿透/击穿解决方案）''细化为：''深入应用Redis：① 缓存穿透→布隆过滤器预检+空值缓存；② 缓存击穿→逻辑过期+互斥锁双重防护；③ 缓存雪崩→随机TTL+多级缓存降级''', 3),
(31, 6, '内容', '低', '教育背景与项目经历存在重复，且''mailto:''邮箱格式错误', '删除项目经验模块中冗余的教育背景复述；统一邮箱为标准格式：''Email: 13538528694@163.com''', 4);

-- =====================================================
-- 6. 面试问题表（包含追问功能扩展）
-- =====================================================
CREATE TABLE `interview_question` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `question_text` TEXT NOT NULL COMMENT '问题文本',
  `question_type` VARCHAR(80) NULL COMMENT '问题类型',
  `category` VARCHAR(120) NULL COMMENT '类别',
  `user_answer` MEDIUMTEXT NULL COMMENT '用户回答',
  `follow_up_question` TEXT NULL COMMENT '追问问题',
  `follow_up_answer` MEDIUMTEXT NULL COMMENT '追问答案',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_interview_question_session_id` (`resume_session_id`),
  KEY `idx_interview_question_type` (`question_type`),
  KEY `idx_interview_question_user_answer` (`user_answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试问题表';

-- 插入面试问题数据
INSERT INTO `interview_question` (`id`, `resume_session_id`, `question_text`, `question_type`, `category`, `sort_order`) VALUES
(21, 1, '你在电商系统中使用 Redis 做缓存，请具体说明缓存了哪些数据？是读多写少的热点数据吗？缓存粒度是接口级、对象级还是字段级？', 'PROJECT', 'Redis - 缓存设计', 0),
(22, 1, '电商系统中用户模块的登录注册功能，是否涉及密码存储？你如何保证密码安全性？是否使用了 Spring Security 或自定义加密方案？', 'PROJECT', 'Spring - 安全机制', 1),
(23, 1, '图书管理系统使用 MyBatis 实现图书增删改查，如果某次更新操作需要同时修改图书信息和关联的库存记录，你是如何保证事务一致性的？是否显式使用了 @Transactional？传播行为和隔离级别是如何设置的？', 'PROJECT', 'MySQL - 事务管理', 2),
(24, 1, '你说''了解 JVM 原理''，请解释 Java 对象在堆内存中的布局结构，并说明对象头中包含哪些关键信息（如 Mark Word、Klass Pointer）？', 'JAVA_BASIC', 'JVM - 对象内存布局', 3),
(25, 1, 'HashMap 在 JDK 1.8 中何时会将链表转为红黑树？为什么阈值是 8？这个设计背后考虑了哪些统计学依据？', 'JAVA_COLLECTION', 'Java集合 - HashMap 底层实现', 4);

-- =====================================================
-- 7. 评估类别分数表
-- =====================================================
CREATE TABLE `evaluation_category_score` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `category` VARCHAR(120) NOT NULL COMMENT '类别',
  `score` INT NULL COMMENT '分数',
  `question_count` INT NULL COMMENT '问题数量',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_eval_category_session_id` (`resume_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估类别分数表';

-- =====================================================
-- 8. 评估改进建议表
-- =====================================================
CREATE TABLE `evaluation_improvement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `improvement_text` VARCHAR(1000) NOT NULL COMMENT '改进建议文本',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_eval_improvement_session_id` (`resume_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估改进建议表';

-- =====================================================
-- 9. 评估问题详情表
-- =====================================================
CREATE TABLE `evaluation_question_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `question_index` INT NOT NULL COMMENT '问题索引',
  `question_text` TEXT NOT NULL COMMENT '问题文本',
  `category` VARCHAR(120) NULL COMMENT '类别',
  `user_answer` MEDIUMTEXT NULL COMMENT '用户回答',
  `score` INT NULL COMMENT '分数',
  `feedback` TEXT NULL COMMENT '反馈',
  PRIMARY KEY (`id`),
  KEY `idx_eval_question_session_id` (`resume_session_id`),
  KEY `idx_eval_question_index` (`question_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估问题详情表';

-- =====================================================
-- 10. 评估优势表
-- =====================================================
CREATE TABLE `evaluation_strength` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `strength_text` VARCHAR(1000) NOT NULL COMMENT '优势文本',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_eval_strength_session_id` (`resume_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估优势表';

-- =====================================================
-- 11. 评估参考答案表
-- =====================================================
CREATE TABLE `evaluation_reference_answer` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `resume_session_id` BIGINT UNSIGNED NOT NULL COMMENT '简历会话ID',
  `question_index` INT NOT NULL COMMENT '问题索引',
  `question_text` TEXT NOT NULL COMMENT '问题文本',
  `reference_answer` MEDIUMTEXT NOT NULL COMMENT '参考答案',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_eval_ref_answer_session_id` (`resume_session_id`),
  KEY `idx_eval_ref_answer_index` (`question_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估参考答案表';

-- =====================================================
-- 12. 评估参考关键点表
-- =====================================================
CREATE TABLE `evaluation_reference_key_point` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reference_answer_id` BIGINT UNSIGNED NOT NULL COMMENT '参考答案ID',
  `key_point` VARCHAR(1000) NOT NULL COMMENT '关键点',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_eval_key_point_answer_id` (`reference_answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估参考关键点表';

-- =====================================================
-- 13. 简历历史视图
-- =====================================================
CREATE OR REPLACE VIEW `v_resume_history` AS
SELECT
    rs.id,
    rs.resume_id,
    rs.user_id,
    rs.position_type,
    rs.status,
    rs.resume_overall_score,
    rs.evaluation_overall_score,
    rs.created_at,
    rs.updated_at,
    (SELECT COUNT(*) FROM interview_question iq WHERE iq.resume_session_id = rs.id) AS question_count
FROM resume_session rs;

-- =====================================================
-- 数据验证
-- =====================================================
SELECT 'User table:' AS '';
SELECT COUNT(*) AS user_count FROM user;

SELECT 'User session table:' AS '';
SELECT COUNT(*) AS user_session_count FROM user_session;

SELECT 'Resume session table:' AS '';
SELECT COUNT(*) AS resume_session_count FROM resume_session;

SELECT 'Resume strength table:' AS '';
SELECT COUNT(*) AS resume_strength_count FROM resume_strength;

SELECT 'Resume suggestion table:' AS '';
SELECT COUNT(*) AS resume_suggestion_count FROM resume_suggestion;

SELECT 'Interview question table:' AS '';
SELECT COUNT(*) AS interview_question_count FROM interview_question;

SELECT 'All tables created and data inserted successfully!' AS '';
