package org.pacos.core.component.notification.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_NOTIFICATION_OPERATION_EVENT")
@Getter
@Setter
@SequenceGenerator(name = "notification-operation-event-seq-gen",
        sequenceName = "APP_NOTIFICATION_OPERATION_EVENT_SEQ", allocationSize = 1)
public class NotificationOperationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification-operation-event-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private NotificationOperation operation;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public NotificationOperationEvent() {
        // Required by JPA.
    }

    public NotificationOperationEvent(NotificationOperation operation, String eventType, String message) {
        this.operation = operation;
        this.eventType = eventType;
        this.message = message;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
