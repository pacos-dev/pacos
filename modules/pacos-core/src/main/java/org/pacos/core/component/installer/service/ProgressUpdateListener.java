package org.pacos.core.component.installer.service;

@FunctionalInterface
public interface ProgressUpdateListener {

    void progressChanged(double value);
}
