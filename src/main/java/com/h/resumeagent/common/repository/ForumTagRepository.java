package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.ForumTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumTagRepository extends JpaRepository<ForumTagEntity, Long> {

    List<ForumTagEntity> findByIdIn(List<Long> ids);
}
