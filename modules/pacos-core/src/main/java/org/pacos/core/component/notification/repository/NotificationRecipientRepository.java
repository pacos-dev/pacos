package org.pacos.core.component.notification.repository;

import java.util.List;

import org.pacos.core.component.notification.domain.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    List<NotificationRecipient> findByNotificationId(Long notificationId);

    void deleteByNotificationId(Long notificationId);
}
