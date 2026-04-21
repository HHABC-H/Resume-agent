# 论坛模块设计文档

## 1. 概述

在现有 Resume-agent 项目基础上扩展论坛功能，为用户提供技术交流社区平台。

## 2. 数据库设计

### 2.1 ER 图

```
forum_category (1) ─── (n) forum_post (n) ─── (n) forum_tag
                                           │
                                           ▼
                                    forum_post_tag
                                           │
                                           ▼
                                      forum_tag
                                           │
forum_essence (n) ─────────────────────────┘
                                           │
forum_comment (n) ─── (1) forum_post ──────┘
      │
      └─── (self) parent_id (二级评论)
```

### 2.2 表结构

| 表名 | 说明 |
|------|------|
| `forum_category` | 分类表 |
| `forum_post` | 帖子表 |
| `forum_comment` | 评论表 |
| `forum_tag` | 标签表 |
| `forum_post_tag` | 帖子-标签关联表 |
| `forum_essence` | 精华帖表 |

### 2.3 字段设计

#### forum_category
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名 |
| description | VARCHAR(200) | 描述 |
| sort_order | INT | 排序 |
| post_count | INT | 帖子数 |

#### forum_post
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| author_id | BIGINT | 作者ID |
| category_id | BIGINT | 分类ID |
| view_count | INT | 浏览数 |
| like_count | INT | 点赞数 |
| dislike_count | INT | 点踩数 |
| comment_count | INT | 评论数 |
| status | TINYINT | 状态(0正常1精华2置顶) |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### forum_comment
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| post_id | BIGINT | 帖子ID |
| parent_id | BIGINT | 父评论ID(0或二级评论ID) |
| author_id | BIGINT | 作者ID |
| content | TEXT | 内容 |
| like_count | INT | 点赞数 |
| dislike_count | INT | 点踩数 |
| created_at | DATETIME | 创建时间 |

#### forum_tag
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(30) | 标签名 |

#### forum_post_tag
| 字段 | 类型 | 说明 |
|------|------|------|
| post_id | BIGINT | 帖子ID |
| tag_id | BIGINT | 标签ID |

#### forum_essence
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| post_id | BIGINT | 帖子ID |
| operator_id | BIGINT | 操作人ID |
| created_at | DATETIME | 设为精华时间 |

## 3. 页面结构

| 路由 | 说明 |
|------|------|
| /forum | 论坛首页(帖子列表) |
| /forum/post/{id} | 帖子详情(含评论) |
| /forum/publish | 发布帖子 |
| /forum/category/{id} | 按分类查看 |
| /forum/essences | 精华帖列表 |

## 4. 核心功能

### 4.1 帖子功能
- 富文本编辑器(支持富文本切换)
- 标题、内容、分类、标签
- 浏览数累计
- 点赞/踩(直接累计到 like_count/dislike_count)
- 评论数累计

### 4.2 评论功能
- 二级评论(parent_id 指向一级评论ID)
- 点赞/踩
- 按时间排序

### 4.3 标签与分类
- 多标签选择
- 分类筛选
- 标签筛选

### 4.4 精华与置顶
- 管理员可设置精华/置顶
- 精华帖单独页面展示

## 5. 技术方案

### 5.1 目录结构
```
src/main/java/com/h/resumeagent/
├── controller/
│   └── ForumController.java
├── service/
│   ├── ForumService.java
│   └── impl/ForumServiceImpl.java
├── entity/
│   ├── ForumPostEntity.java
│   ├── ForumCommentEntity.java
│   ├── ForumCategoryEntity.java
│   ├── ForumTagEntity.java
│   └── ForumEssenceEntity.java
├── repository/
│   ├── ForumPostRepository.java
│   ├── ForumCommentRepository.java
│   ├── ForumCategoryRepository.java
│   ├── ForumTagRepository.java
│   └── ForumEssenceRepository.java
└── dto/
    └── (相关 DTO)

src/main/resources/
├── templates/forum/
│   ├── index.html
│   ├── post-detail.html
│   ├── publish.html
│   ├── category.html
│   └── essences.html
├── mapper/
│   └── ForumMapper.xml
└── forum.sql
```

### 5.2 安全
- 登录用户才能发帖/评论
- 管理员(ROLE_ADMIN)才能设置精华/置顶

## 6. 实施顺序

1. 数据库表创建
2. Entity/Repository 层
3. Service 层(含 CRUD)
4. Controller 层
5. 前端页面
6. 管理员精华/置顶功能
