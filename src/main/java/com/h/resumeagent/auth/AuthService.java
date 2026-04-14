package com.h.resumeagent.auth;

import com.h.resumeagent.persistence.entity.UserEntity;
import com.h.resumeagent.persistence.entity.UserSessionEntity;
import com.h.resumeagent.persistence.repository.UserRepository;
import com.h.resumeagent.persistence.repository.UserSessionRepository;
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
public class AuthService {

    private static final int USER_STATUS_ACTIVE = 1;
    private static final long DEFAULT_EXPIRE_DAYS = 7;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public AuthService(UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
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
                .status(USER_STATUS_ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    @Transactional
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
    public Optional<UserEntity> findUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    private AuthenticatedUser toAuthenticatedUser(UserEntity user) {
        return new AuthenticatedUser(user.getId(), user.getUsername());
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

    public record LoginResult(String token, LocalDateTime expiresAt, AuthenticatedUser user) {
    }
}
