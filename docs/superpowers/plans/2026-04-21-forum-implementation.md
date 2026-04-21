# 论坛模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Resume-agent 项目上新增论坛模块，支持帖子发布、评论、点赞/踩、精华/置顶功能

**Architecture:** 采用现有项目分层结构（Controller-Service-Repository-Entity），数据库使用 MySQL，JPA 管理实体，Thymeleaf 渲染前端

**Tech Stack:** Spring Boot 3.5 + JPA + MySQL + Thymeleaf

---

## 文件结构

```
src/main/java/com/h/resumeagent/
├── controller/ForumController.java           # 论坛控制器
├── service/ForumService.java                 # 论坛服务接口
├── service/impl/ForumServiceImpl.java        # 论坛服务实现
├── common/entity/
│   ├── ForumPostEntity.java                  # 帖子实体
│   ├── ForumCommentEntity.java               # 评论实体
│   ├── ForumCategoryEntity.java              # 分类实体
│   ├── ForumTagEntity.java                   # 标签实体
│   ├── ForumPostTagEntity.java               # 帖子-标签关联实体
│   └── ForumEssenceEntity.java               # 精华帖实体
├── common/repository/
│   ├── ForumPostRepository.java
│   ├── ForumCommentRepository.java
│   ├── ForumCategoryRepository.java
│   ├── ForumTagRepository.java
│   └── ForumEssenceRepository.java
└── common/dto/
    ├── ForumPostDTO.java                     # 帖子列表项 DTO
    ├── ForumPostDetailDTO.java               # 帖子详情 DTO
    ├── ForumCommentDTO.java                  # 评论 DTO
    └── CreatePostRequest.java                # 创建帖子请求

src/main/resources/
├── templates/forum/                          # 论坛页面
│   ├── index.html                           # 论坛首页
│   ├── post-detail.html                     # 帖子详情
│   ├── publish.html                         # 发布帖子
│   ├── category.html                        # 分类帖子列表
│   └── essences.html                        # 精华帖列表
├── mapper/ForumMapper.xml                   # MyBatis 映射（如需复杂查询）
└── forum.sql                                # 数据库初始化脚本
```

---

## Task 1: 数据库表创建

**Files:**
- Create: `src/main/resources/forum.sql`

```sql
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
```

- [ ] **Step 1: 创建 forum.sql**

运行: `mysql -u root -p resume_agent < src/main/resources/forum.sql` 或手动执行

---

## Task 2: 实体类创建

**Files:**
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumCategoryEntity.java`
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumTagEntity.java`
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumPostEntity.java`
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumPostTagEntity.java`
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumCommentEntity.java`
- Create: `src/main/java/com/h/resumeagent/common/entity/ForumEssenceEntity.java`

- [ ] **Step 1: 创建 ForumCategoryEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_category")
public class ForumCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "post_count")
    private Integer postCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = java.time.LocalDateTime.now();
        if (postCount == null) postCount = 0;
        if (sortOrder == null) sortOrder = 0;
    }
}
```

- [ ] **Step 2: 创建 ForumTagEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_tag")
public class ForumTagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;
}
```

- [ ] **Step 3: 创建 ForumPostEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_post", indexes = {
    @Index(name = "idx_forum_post_author", columnList = "author_id"),
    @Index(name = "idx_forum_post_category", columnList = "category_id"),
    @Index(name = "idx_forum_post_status", columnList = "status")
})
public class ForumPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "dislike_count")
    @Builder.Default
    private Integer dislikeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ForumCategoryEntity category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ForumPostTagEntity> postTags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (viewCount == null) viewCount = 0;
        if (likeCount == null) likeCount = 0;
        if (dislikeCount == null) dislikeCount = 0;
        if (commentCount == null) commentCount = 0;
        if (status == null) status = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 4: 创建 ForumPostTagEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_post_tag")
@IdClass(ForumPostTagId.class)
public class ForumPostTagEntity {
    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "tag_id")
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private ForumTagEntity tag;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ForumPostTagId implements Serializable {
    private Long postId;
    private Long tagId;
}
```

- [ ] **Step 5: 创建 ForumCommentEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_comment", indexes = {
    @Index(name = "idx_forum_comment_post", columnList = "post_id"),
    @Index(name = "idx_forum_comment_parent", columnList = "parent_id")
})
public class ForumCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_id")
    @Builder.Default
    private Long parentId = 0L;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "dislike_count")
    @Builder.Default
    private Integer dislikeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPostEntity post;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (dislikeCount == null) dislikeCount = 0;
        if (parentId == null) parentId = 0L;
    }
}
```

- [ ] **Step 6: 创建 ForumEssenceEntity.java**

```java
package com.h.resumeagent.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_essence")
public class ForumEssenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false, unique = true)
    private Long postId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPostEntity post;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
```

---

## Task 3: Repository 创建

**Files:**
- Create: `src/main/java/com/h/resumeagent/common/repository/ForumPostRepository.java`
- Create: `src/main/java/com/h/resumeagent/common/repository/ForumCommentRepository.java`
- Create: `src/main/java/com/h/resumeagent/common/repository/ForumCategoryRepository.java`
- Create: `src/main/java/com/h/resumeagent/common/repository/ForumTagRepository.java`
- Create: `src/main/java/com/h/resumeagent/common/repository/ForumEssenceRepository.java`

- [ ] **Step 1: 创建 ForumPostRepository.java**

```java
package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPostEntity, Long> {

    Page<ForumPostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ForumPostEntity> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    Page<ForumPostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<ForumPostEntity> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    @Query("SELECT p FROM ForumPostEntity p WHERE p.status > 0 ORDER BY p.createdAt DESC")
    Page<ForumPostEntity> findEssences(Pageable pageable);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.dislikeCount = p.dislikeCount + 1 WHERE p.id = :id")
    void incrementDislikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumPostEntity p SET p.status = :status WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
```

- [ ] **Step 2: 创建 ForumCommentRepository.java**

```java
package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumCommentEntity, Long> {

    List<ForumCommentEntity> findByPostIdAndParentIdOrderByCreatedAtAsc(Long postId, Long parentId);

    List<ForumCommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCommentEntity c SET c.dislikeCount = c.dislikeCount + 1 WHERE c.id = :id")
    void incrementDislikeCount(@Param("id") Long id);
}
```

- [ ] **Step 3: 创建 ForumCategoryRepository.java**

```java
package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategoryEntity, Long> {

    @Modifying
    @Query("UPDATE ForumCategoryEntity c SET c.postCount = c.postCount + 1 WHERE c.id = :id")
    void incrementPostCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ForumCategoryEntity c SET c.postCount = c.postCount - 1 WHERE c.id = :id AND c.postCount > 0")
    void decrementPostCount(@Param("id") Long id);
}
```

- [ ] **Step 4: 创建 ForumTagRepository.java**

```java
package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumTagRepository extends JpaRepository<ForumTagEntity, Long> {

    List<ForumTagEntity> findByIdIn(List<Long> ids);
}
```

- [ ] **Step 5: 创建 ForumEssenceRepository.java**

```java
package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumEssenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumEssenceRepository extends JpaRepository<ForumEssenceEntity, Long> {

    Optional<ForumEssenceEntity> findByPostId(Long postId);

    boolean existsByPostId(Long postId);
}
```

---

## Task 4: DTO 创建

**Files:**
- Create: `src/main/java/com/h/resumeagent/common/dto/ForumPostDTO.java`
- Create: `src/main/java/com/h/resumeagent/common/dto/ForumPostDetailDTO.java`
- Create: `src/main/java/com/h/resumeagent/common/dto/ForumCommentDTO.java`
- Create: `src/main/java/com/h/resumeagent/common/dto/CreatePostRequest.java`

- [ ] **Step 1: 创建 ForumPostDTO.java**

```java
package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForumPostDTO {
    private Long id;
    private String title;
    private String contentPreview;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Integer status;
    private List<String> tags;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建 ForumPostDetailDTO.java**

```java
package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForumPostDetailDTO {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Integer status;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ForumCommentDTO> comments;
}
```

- [ ] **Step 3: 创建 ForumCommentDTO.java**

```java
package com.h.resumeagent.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ForumCommentDTO {
    private Long id;
    private Long postId;
    private Long parentId;
    private Long authorId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建 CreatePostRequest.java**

```java
package com.h.resumeagent.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreatePostRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;

    private List<Long> tagIds;
}
```

---

## Task 5: Service 创建

**Files:**
- Create: `src/main/java/com/h/resumeagent/service/ForumService.java`
- Create: `src/main/java/com/h/resumeagent/service/impl/ForumServiceImpl.java`

- [ ] **Step 1: 创建 ForumService.java**

```java
package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ForumService {
    Page<ForumPostDTO> getPosts(Pageable pageable);
    Page<ForumPostDTO> getPostsByCategory(Long categoryId, Pageable pageable);
    Page<ForumPostDTO> getPostsByAuthor(Long authorId, Pageable pageable);
    Page<ForumPostDTO> getEssences(Pageable pageable);
    ForumPostDetailDTO getPostDetail(Long postId, Long userId);
    ForumPostDTO createPost(CreatePostRequest request, Long authorId);
    void deletePost(Long postId, Long userId);
    void incrementViewCount(Long postId);

    ForumCommentDTO createComment(Long postId, Long parentId, String content, Long authorId);
    List<ForumCommentDTO> getComments(Long postId);

    void likePost(Long postId);
    void dislikePost(Long postId);
    void likeComment(Long commentId);
    void dislikeComment(Long commentId);

    void setEssence(Long postId, Long operatorId);
    void removeEssence(Long postId);
    void setTop(Long postId, Long operatorId);
    void removeTop(Long postId);

    List<ForumCategoryDTO> getCategories();
}

@Data
@Builder
class ForumCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer postCount;
}
```

- [ ] **Step 2: 创建 ForumServiceImpl.java**

实现包含：
- 帖子 CRUD
- 评论 CRUD（含二级评论）
- 点赞/踩（直接更新 like_count/dislike_count）
- 精华/置顶设置（需管理员权限校验）
- 浏览数累计

---

## Task 6: Controller 创建

**Files:**
- Create: `src/main/java/com/h/resumeagent/controller/ForumController.java`

- [ ] **Step 1: 创建 ForumController.java**

路由设计：
- `GET /forum` - 论坛首页
- `GET /forum/post/{id}` - 帖子详情
- `GET /forum/publish` - 发布页面
- `POST /forum/post` - 创建帖子
- `DELETE /forum/post/{id}` - 删除帖子
- `POST /forum/post/{id}/like` - 点赞
- `POST /forum/post/{id}/dislike` - 点踩
- `POST /forum/comment` - 创建评论
- `POST /forum/comment/{id}/like` - 评论点赞
- `POST /forum/comment/{id}/dislike` - 评论点踩
- `GET /forum/category/{id}` - 分类帖子
- `GET /forum/essences` - 精华帖
- `POST /forum/admin/essence/{id}` - 设置精华
- `POST /forum/admin/top/{id}` - 设置置顶

---

## Task 7: 前端页面创建

**Files:**
- Create: `src/main/resources/templates/forum/index.html`
- Create: `src/main/resources/templates/forum/post-detail.html`
- Create: `src/main/resources/templates/forum/publish.html`
- Create: `src/main/resources/templates/forum/category.html`
- Create: `src/main/resources/templates/forum/essences.html`

- [ ] **Step 1: 创建 index.html** - 论坛首页（帖子列表、分页、分类侧边栏）
- [ ] **Step 2: 创建 post-detail.html** - 帖子详情（含评论、点赞/踩）
- [ ] **Step 3: 创建 publish.html** - 发布帖子页面（富文本编辑器）
- [ ] **Step 4: 创建 category.html** - 分类帖子列表
- [ ] **Step 5: 创建 essences.html** - 精华帖列表

---

## Task 8: 导航栏添加论坛入口

**Files:**
- Modify: `src/main/resources/templates/` 下各页面添加论坛入口链接

- [ ] **Step 1: 修改导航栏** - 在现有页面的导航栏添加"论坛"链接

---

## Task 9: 提交代码

- [ ] **Step 1: 提交论坛模块代码**

```bash
git add -A && git commit -m "feat: add forum module with posts, comments, likes, essence and top features"
```
