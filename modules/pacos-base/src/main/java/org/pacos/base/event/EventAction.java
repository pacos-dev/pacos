package org.pacos.base.event;

@FunctionalInterface
public interface EventAction<T> {

    void fireEvent(T t);
}
