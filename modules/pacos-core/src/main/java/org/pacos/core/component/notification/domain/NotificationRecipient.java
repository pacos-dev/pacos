package org.pacos.core.component.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Explicit recipient row for notifications not intended for all users.
 */
@Entity
@Table(name = "APP_NOTIFICATION_RECIPIENT",
        uniqueConstraints = @UniqueConstraint(name = "UQ_NOTIFICATION_RECIPIENT",
                columnNames = {"notification_id", "user_id"}))
@Getter
@Setter
@SequenceGenerator(name = "notification-recipient-seq-gen",
        sequenceName = "APP_NOTIFICATION_RECIPIENT_SEQ", allocationSize = 1)
public class NotificationRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification-recipient-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    public NotificationRecipient() {
        // Required by JPA.
    }

    public NotificationRecipient(Long notificationId, Integer userId) {
        this.notificationId = notificationId;
        this.userId = userId;
    }
}
