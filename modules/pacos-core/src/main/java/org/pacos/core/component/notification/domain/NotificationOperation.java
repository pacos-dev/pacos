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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_NOTIFICATION_OPERATION")
@Getter
@Setter
@SequenceGenerator(name = "notification-operation-seq-gen",
        sequenceName = "APP_NOTIFICATION_OPERATION_SEQ", allocationSize = 1)
public class NotificationOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification-operation-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "summary", length = 1000)
    private String summary;

    @Column(name = "details", length = 12000)
    private String details;

    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 32)
    private NotificationPriority priority = NotificationPriority.INFO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private NotificationOperationStatus status = NotificationOperationStatus.PENDING;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "current_step", length = 255)
    private String currentStep;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public NotificationOperation() {
        // Required by JPA.
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (sourceKey == null || sourceKey.isBlank()) {
            sourceKey = "SYSTEM";
        }
        if (priority == null) {
            priority = NotificationPriority.INFO;
        }
        if (status == null) {
            status = NotificationOperationStatus.PENDING;
        }
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
