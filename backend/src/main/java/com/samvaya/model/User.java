package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "account_status", length = 30)
    @Builder.Default
    private String accountStatus = "ACTIVE"; // 'ACTIVE', 'INACTIVE', 'OFFBOARDED'

    @Column(name = "resident_type", length = 30)
    private String residentType; // 'OWNER', 'TENANT'

    @Column(name = "moved_out_at")
    private LocalDateTime movedOutAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        syncStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        syncStatus();
    }

    private void syncStatus() {
        if ("INACTIVE".equalsIgnoreCase(this.accountStatus) || "OFFBOARDED".equalsIgnoreCase(this.accountStatus)) {
            this.isActive = false;
        } else if (Boolean.FALSE.equals(this.isActive)) {
            this.accountStatus = "INACTIVE";
        }
    }
}
