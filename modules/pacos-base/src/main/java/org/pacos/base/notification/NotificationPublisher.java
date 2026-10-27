package org.pacos.base.notification;

/**
 * Public publication contract for PacOS modules. Implementations own persistence,
 * deduplication and delivery; publishers should not depend on pacos-core or Vaadin.
 */
@FunctionalInterface
public interface NotificationPublisher {

    NotificationPublishResult publish(NotificationRequest request);
}
