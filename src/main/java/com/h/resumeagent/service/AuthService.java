package com.h.resumeagent.service;

import com.h.resumeagent.common.dto.AuthenticatedUser;
import com.h.resumeagent.common.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthService {

    UserEntity register(String username, String email, String rawPassword, String displayName);

    LoginResult login(String username, String rawPassword);

    Optional<AuthenticatedUser> authenticate(String rawToken);

    void logout(String rawToken);

    Optional<UserEntity> findUserById(Long id);

    UserEntity updateUser(Long id, String email, String displayName);

    void updateUserStatus(Long id, int status);

    void updateUserRole(Long id, String role);

    record LoginResult(String token, LocalDateTime expiresAt, AuthenticatedUser user) {
    }
}