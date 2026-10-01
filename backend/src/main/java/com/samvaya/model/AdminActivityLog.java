package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String action; // 'CREATED', 'UPDATED', 'DELETED', 'APPROVED', 'REJECTED', 'PUBLISHED', 'DEACTIVATED'

    @Column(nullable = false, length = 50)
    private String module; // 'RESIDENTS', 'FLATS', 'STAFF', 'MAINTENANCE', 'COMPLAINTS', 'AMENITIES', 'NOTICES', 'USERS'

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
