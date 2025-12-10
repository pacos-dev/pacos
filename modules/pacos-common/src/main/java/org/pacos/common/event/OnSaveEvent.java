package org.pacos.common.event;
@FunctionalInterface
public interface OnSaveEvent<T> {

    void onSaveEvent(T t);
}
