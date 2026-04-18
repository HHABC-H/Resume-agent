package com.h.resumeagent.common.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`user`", indexes = {
                @Index(name = "uk_user_username", columnList = "username", unique = true),
                @Index(name = "uk_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_role", columnList = "role")
})
public class UserEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "username", nullable = false, length = 64)
        private String username;

        @Column(name = "email", length = 255)
        private String email;

        @Column(name = "password_hash", nullable = false, length = 255)
        private String passwordHash;

        @Column(name = "display_name", length = 100)
        private String displayName;

        @Column(name = "role", nullable = false, length = 32)
        private String role;

        @Column(name = "status", nullable = false)
        private Integer status;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Builder.Default
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<UserSessionEntity> sessions = new ArrayList<>();
}
