package org.pacos.core.component.plugin.event;

import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;

/**
 * Used to monitor state of installation process
 */
@FunctionalInterface
public interface InstallationStatusListener {

    void stateChanged(DownloadPluginStatus status);

}