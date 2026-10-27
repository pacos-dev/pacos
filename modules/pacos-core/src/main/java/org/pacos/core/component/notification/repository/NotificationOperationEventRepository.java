package org.pacos.core.component.notification.repository;

import java.util.List;

import org.pacos.core.component.notification.domain.NotificationOperationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationOperationEventRepository extends JpaRepository<NotificationOperationEvent, Long> {
    List<NotificationOperationEvent> findByOperation_IdOrderByCreatedAtAsc(Long operationId);
}
