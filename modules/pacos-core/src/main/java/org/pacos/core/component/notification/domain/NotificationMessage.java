package org.pacos.core.component.notification.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_NOTIFICATION_MESSAGE")
@Getter
@Setter
@SequenceGenerator(name = "notification-message-seq-gen",
        sequenceName = "APP_NOTIFICATION_MESSAGE_SEQ", allocationSize = 1)
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification-message-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "summary", length = 1000)
    private String summary;

    @Column(name = "body", length = 12000)
    private String body;

    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private NotificationCategory category = NotificationCategory.SYSTEM;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private NotificationPriority priority = NotificationPriority.INFO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "action_url", length = 2048)
    private String actionUrl;

    @Column(name = "requires_acknowledgement", nullable = false)
    private boolean requiresAcknowledgement;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public NotificationMessage() {
        // Required by JPA.
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (category == null) {
            category = NotificationCategory.SYSTEM;
        }
        if (priority == null) {
            priority = NotificationPriority.INFO;
        }
        if (sourceKey == null || sourceKey.isBlank()) {
            sourceKey = "SYSTEM";
        }
    }
}
