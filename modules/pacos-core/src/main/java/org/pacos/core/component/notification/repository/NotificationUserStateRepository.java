package org.pacos.core.component.notification.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationUserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUserStateRepository extends JpaRepository<NotificationUserState, Long> {
    Optional<NotificationUserState> findByUserIdAndItemTypeAndItemId(
            Integer userId, NotificationItemType itemType, Long itemId);

    List<NotificationUserState> findAllByUserIdAndItemTypeAndItemIdIn(
            Integer userId, NotificationItemType itemType, Collection<Long> itemIds);

    void deleteByUserIdAndItemTypeAndItemId(Integer userId, NotificationItemType itemType, Long itemId);
}
