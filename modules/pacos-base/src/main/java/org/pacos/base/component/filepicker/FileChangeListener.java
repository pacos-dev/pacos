package org.pacos.base.component.filepicker;
@FunctionalInterface
public interface FileChangeListener<T> {
    void valueEvent(T t);
}
