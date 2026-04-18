package com.h.resumeagent.common.repository;

import com.h.resumeagent.common.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {

    Optional<UserSessionEntity> findBySessionToken(String sessionToken);

    Optional<UserSessionEntity> findBySessionTokenAndRevokedAtIsNullAndExpiresAtAfter(
            String sessionToken,
            LocalDateTime now);
}
