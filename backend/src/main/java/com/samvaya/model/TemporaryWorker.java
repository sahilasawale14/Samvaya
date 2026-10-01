package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(nullable = false)
    private String purpose;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Column(length = 100)
    private String area;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private User guard;

    @Column(length = 30)
    @Builder.Default
    private String status = "INSIDE"; // 'EXPECTED', 'INSIDE', 'EXITED'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.entryTime == null) {
            this.entryTime = LocalDateTime.now();
        }
    }
}
