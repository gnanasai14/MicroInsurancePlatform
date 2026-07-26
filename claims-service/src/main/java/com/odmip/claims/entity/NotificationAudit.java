package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long claimId;

    private String eventType;

    private String topicArn;

    private String messageId;

    private String status; // SENT, FAILED, MOCKED

    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
