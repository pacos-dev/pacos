package org.pacos.core.component.plugin.view.plugin;

public enum DownloadPluginStatus {
    DOWNLOADING, INSTALLING, ERROR, FINISHED;

    public boolean isFinished() {
        return this == FINISHED;
    }
}
