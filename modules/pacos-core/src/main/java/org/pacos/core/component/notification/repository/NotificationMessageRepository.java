package org.pacos.core.component.notification.repository;

import java.util.List;

import org.pacos.core.component.notification.domain.NotificationMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
    List<NotificationMessage> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
}
