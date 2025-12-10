package org.pacos.common.event;

@FunctionalInterface
public interface ColumnFilterEvent {
    void onFilterSet(String value);
}
