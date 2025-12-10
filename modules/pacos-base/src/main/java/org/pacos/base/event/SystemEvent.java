package org.pacos.base.event;

import com.vaadin.flow.component.Component;

/**
 * It introduces basic plugin management functionality.
 * Provides an event system and access to application sessions
 */
public class SystemEvent<T extends EventType> {

    private final EventPublisher<T> publisher;

    protected SystemEvent() {
        this.publisher = new EventPublisher<>();
    }

    /**
     * Listener for given event type will be added immediately to the listeners queue.
     * Unregister must be done manually by 'unsubscribe' method
     *
     * @param event value object for even type and Notifier
     */
    public void subscribe(Event<T> event) {
        publisher.subscribe(event.eventType(),event.notifier());
    }
    public void unsubscribe(Event<T> event) {
        publisher.unsubscribe(event.eventType(),event.notifier());
    }
    public void subscribe(T eventType, Notifier notifier) {
        publisher.subscribe(eventType, notifier);
    }

    public void unsubscribe(T eventType, Notifier notifier) {
        publisher.unsubscribe(eventType, notifier);
    }

    /**
     * Add subscription when given component is attached to UI, and automatically remove them when detached to
     * prevent memory leak
     *
     * @param eventType Event type
     * @param notifier Logic called on event
     */
    public void subscribeOnAttached(Component c,T eventType, Notifier notifier) {
        if(c.isAttached()){
            throw new UnsupportedOperationException("Component is already attached");
        }
        c.addAttachListener(e->subscribe(eventType, notifier));
        c.addDetachListener(e->unsubscribe(eventType, notifier));
    }
    public void notify(T eventType, Object o) {
        publisher.notify(eventType, o);
    }

    public void notify(T eventType) {
        publisher.notify(eventType);
    }
}
