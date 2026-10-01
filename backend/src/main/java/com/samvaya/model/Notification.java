package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false, length = 50)
    private String type; // 'VISITOR', 'DELIVERY', 'COMPLAINT', 'SERVICE_REQUEST', 'PAYMENT', 'AMENITY', 'NOTICE', 'EMERGENCY', 'SYSTEM'

    @Column(length = 20)
    @Builder.Default
    private String priority = "NORMAL"; // 'LOW', 'NORMAL', 'HIGH', 'URGENT'

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
