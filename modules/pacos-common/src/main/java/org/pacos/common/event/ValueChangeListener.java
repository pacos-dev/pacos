package org.pacos.common.event;
@FunctionalInterface
public interface ValueChangeListener<T> {
    void valueEvent(T t);
}
