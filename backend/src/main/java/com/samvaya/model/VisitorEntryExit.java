package com.samvaya.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_entry_exits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorEntryExit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id", nullable = false)
    private Visitor visitor;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private User guard;

    @Column(name = "gate_number", length = 20)
    @Builder.Default
    private String gateNumber = "Main Gate 1";

    @Column(name = "verification_notes")
    private String verificationNotes;

    @PrePersist
    protected void onCreate() {
        if (this.entryTime == null) {
            this.entryTime = LocalDateTime.now();
        }
    }
}
