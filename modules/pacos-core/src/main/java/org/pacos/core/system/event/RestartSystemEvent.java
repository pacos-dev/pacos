package org.pacos.core.system.event;

import java.rmi.RemoteException;

import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.process.StarterRecoveryService;
import org.pacos.config.rmi.RemoteRegistryService;
import org.pacos.config.rmi.RemoteRestartCInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestartSystemEvent {

    private static final Logger LOG = LoggerFactory.getLogger(RestartSystemEvent.class);
    private static final long RECOVERY_TIMEOUT_MILLIS = 15_000;
    private static final long RECOVERY_POLL_INTERVAL_MILLIS = 250;

    private RestartSystemEvent() {
    }

    public static boolean fireEvent() {
        try {
            RemoteRestartCInterface remoteInterface = loadOrRecoverStarter();
            LOG.info("Restarting system...");
            remoteInterface.restart();
            return true;
        } catch (Exception ex) {
            LOG.error("Cannot restart PacOS because the starter is unavailable", ex);
            NotificationUtils.error(
                    "Looks like the PacOSRunner application is not running and could not be recovered. Can't restart the server");
            return false;
        }
    }

    private static RemoteRestartCInterface loadOrRecoverStarter() throws Exception {
        try {
            return loadAliveRemoteInterface();
        } catch (Exception initialFailure) {
            LOG.warn("PacOS starter health check failed; trying to recover it in attach mode", initialFailure);
            if (!StarterRecoveryService.startInAttachMode()) {
                throw initialFailure;
            }

            long deadline = System.nanoTime() + RECOVERY_TIMEOUT_MILLIS * 1_000_000;
            Exception lastFailure = initialFailure;
            while (System.nanoTime() < deadline) {
                try {
                    return loadAliveRemoteInterface();
                } catch (Exception e) {
                    lastFailure = e;
                }

                try {
                    Thread.sleep(RECOVERY_POLL_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RemoteException("Interrupted while waiting for starter recovery", e);
                }
            }
            throw lastFailure;
        }
    }

    private static RemoteRestartCInterface loadAliveRemoteInterface() throws Exception {
        RemoteRestartCInterface remoteInterface = RemoteRegistryService.loadRemoteInterface();
        if (!remoteInterface.isAlive()) {
            throw new RemoteException("PacOS starter health check returned false");
        }
        return remoteInterface;
    }
}
