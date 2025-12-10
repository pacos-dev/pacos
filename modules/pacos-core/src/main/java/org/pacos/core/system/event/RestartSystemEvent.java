package org.pacos.core.system.event;

import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.rmi.RemoteRegistryService;
import org.pacos.config.rmi.RemoteRestartCInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class RestartSystemEvent {

    private static final Logger LOG = LoggerFactory.getLogger(RestartSystemEvent.class);

    private RestartSystemEvent() {
    }

    public static boolean fireEvent() {
        try {
            RemoteRestartCInterface remoteInterface = RemoteRegistryService.loadRemoteInterface();
            LOG.info("Restarting system...");
            remoteInterface.restart();
            return true;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            NotificationUtils.error("Looks like the PacOSRunner application is not running. Can't restart the server");
            return false;
        }

    }
}
