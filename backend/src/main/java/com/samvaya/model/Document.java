package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String category; // 'BYLAWS', 'RULES', 'MEETING_MINUTES', 'FINANCIAL_REPORT', 'AUDIT', 'FORM', 'OTHER'

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", length = 50)
    private String fileSize;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 30)
    @Builder.Default
    private String visibility = "ALL"; // 'ALL', 'OWNERS_ONLY', 'ADMIN_ONLY'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedByUser;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
