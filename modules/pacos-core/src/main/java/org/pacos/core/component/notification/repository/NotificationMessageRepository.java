package org.pacos.core.component.notification.repository;

import java.util.List;
import java.util.Optional;

import org.pacos.base.notification.NotificationAudience;
import org.pacos.core.component.notification.domain.NotificationMessage;
import org.pacos.core.component.notification.domain.NotificationRecipient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {

    Optional<NotificationMessage> findBySourceKeyAndBusinessKey(String sourceKey, String businessKey);

    @Query("""
            select message from NotificationMessage message
            where message.active = true
              and (message.audience = :allUsers
                   or message.id in (select recipient.notificationId from NotificationRecipient recipient
                                     where recipient.userId = :userId))
            order by message.createdAt desc
            """)
    List<NotificationMessage> findVisibleToUser(@Param("userId") int userId,
                                                @Param("allUsers") NotificationAudience allUsers,
                                                Pageable pageable);
}
