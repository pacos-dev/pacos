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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_NOTIFICATION_USER_STATE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_type", "item_id"}))
@Getter
@Setter
@SequenceGenerator(name = "notification-user-state-seq-gen",
        sequenceName = "APP_NOTIFICATION_USER_STATE_SEQ", allocationSize = 1)
public class NotificationUserState {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification-user-state-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private NotificationItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    public NotificationUserState() {
        // Required by JPA.
    }

    public NotificationUserState(Integer userId, NotificationItemType itemType, Long itemId) {
        this.userId = userId;
        this.itemType = itemType;
        this.itemId = itemId;
    }

    @PrePersist
    void validateRequiredFields() {
        if (userId == null || itemType == null || itemId == null) {
            throw new IllegalStateException("Notification state requires a user and an item identifier");
        }
    }
}
