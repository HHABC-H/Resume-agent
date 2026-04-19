package com.h.resumeagent.service.impl;

import com.h.resumeagent.common.dto.AuthenticatedUser;
import com.h.resumeagent.common.entity.UserEntity;
import com.h.resumeagent.common.entity.UserSessionEntity;
import com.h.resumeagent.common.repository.UserRepository;
import com.h.resumeagent.common.repository.UserSessionRepository;
import com.h.resumeagent.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int USER_STATUS_ACTIVE = 1;
    private static final int USER_STATUS_DISABLED = 0;
    private static final long DEFAULT_EXPIRE_DAYS = 7;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public AuthServiceImpl(UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    @Override
    public UserEntity register(String username, String email, String rawPassword, String displayName) {
        String normalizedUsername = StringUtils.trimToEmpty(username);
        String normalizedEmail = StringUtils.trimToNull(email);
        String normalizedDisplayName = StringUtils.trimToNull(displayName);

        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 64) {
            throw new IllegalArgumentException("用户名长度需在 3~64 之间");
        }
        if (StringUtils.isBlank(rawPassword) || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度至少 6 位");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .displayName(normalizedDisplayName)
                .role("USER")
                .status(USER_STATUS_ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public LoginResult login(String username, String rawPassword) {
        UserEntity user = userRepository.findByUsername(StringUtils.trimToEmpty(username))
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != USER_STATUS_ACTIVE) {
            throw new IllegalArgumentException("用户已禁用");
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256(token);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(DEFAULT_EXPIRE_DAYS);

        UserSessionEntity session = UserSessionEntity.builder()
                .user(user)
                .sessionToken(tokenHash)
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userSessionRepository.save(session);

        return new LoginResult(token, expiresAt, toAuthenticatedUser(user));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AuthenticatedUser> authenticate(String rawToken) {
        if (StringUtils.isBlank(rawToken)) {
            return Optional.empty();
        }
        String tokenHash = sha256(rawToken);
        return userSessionRepository
                .findBySessionTokenAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, LocalDateTime.now())
                .map(UserSessionEntity::getUser)
                .filter(user -> user.getStatus() != null && user.getStatus() == USER_STATUS_ACTIVE)
                .map(this::toAuthenticatedUser);
    }

    @Transactional
    @Override
    public void logout(String rawToken) {
        if (StringUtils.isBlank(rawToken)) {
            return;
        }
        String tokenHash = sha256(rawToken);
        userSessionRepository.findBySessionToken(tokenHash).ifPresent(session -> {
            session.setRevokedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserEntity> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Transactional
    @Override
    public UserEntity updateUser(Long id, String email, String displayName) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        String normalizedEmail = StringUtils.trimToNull(email);
        String normalizedDisplayName = StringUtils.trimToNull(displayName);
        
        if (normalizedEmail != null && !normalizedEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new IllegalArgumentException("邮箱已存在");
            }
            user.setEmail(normalizedEmail);
        }
        
        if (normalizedDisplayName != null) {
            user.setDisplayName(normalizedDisplayName);
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserStatus(Long id, int status) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (status != USER_STATUS_ACTIVE && status != USER_STATUS_DISABLED) {
            throw new IllegalArgumentException("无效的用户状态");
        }
        
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private AuthenticatedUser toAuthenticatedUser(UserEntity user) {
        return new AuthenticatedUser(user.getId(), user.getUsername(), user.getRole());
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}