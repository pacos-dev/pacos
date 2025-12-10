package org.pacos.base.event;

/**
 * Value object that represents event
 * @param eventType event type
 * @param notifier notifier
 */
public record Event<T extends EventType>(T eventType, Notifier notifier) {

}
