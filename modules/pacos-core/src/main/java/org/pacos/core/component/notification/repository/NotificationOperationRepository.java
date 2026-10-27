package org.pacos.core.component.notification.repository;

import java.util.List;

import org.pacos.core.component.notification.domain.NotificationOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationOperationRepository extends JpaRepository<NotificationOperation, Long> {
    List<NotificationOperation> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
