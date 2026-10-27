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
            RemoteRestartCInterface remoteInterface = loadOrRecoverStarter(
                    RECOVERY_TIMEOUT_MILLIS, RECOVERY_TIMEOUT_MILLIS, RECOVERY_POLL_INTERVAL_MILLIS);
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

    static synchronized RemoteRestartCInterface loadOrRecoverStarter(long attachTimeoutMillis,
                                                                     long fallbackTimeoutMillis,
                                                                     long pollIntervalMillis) throws Exception {
        try {
            return loadAliveRemoteInterface();
        } catch (Exception initialFailure) {
            LOG.warn("PacOS starter health check failed; trying to recover it in attach mode", initialFailure);
            Exception attachFailure = initialFailure;

            if (StarterRecoveryService.startInAttachMode()) {
                try {
                    return waitForAliveRemoteInterface(attachTimeoutMillis, pollIntervalMillis);
                } catch (Exception e) {
                    attachFailure = e;
                    if (Thread.currentThread().isInterrupted()) {
                        throw e;
                    }
                    LOG.warn("Starter attach recovery did not restore RMI", e);
                }
            }

            LOG.warn("RMI recovery by attach failed; starting the starter in recovery mode");
            if (!StarterRecoveryService.startInRecoveryMode()) {
                if (attachFailure != initialFailure) {
                    attachFailure.addSuppressed(initialFailure);
                }
                throw attachFailure;
            }

            try {
                return waitForAliveRemoteInterface(fallbackTimeoutMillis, pollIntervalMillis);
            } catch (Exception fallbackFailure) {
                if (fallbackFailure != attachFailure) {
                    fallbackFailure.addSuppressed(attachFailure);
                }
                throw fallbackFailure;
            }
        }
    }

    private static RemoteRestartCInterface waitForAliveRemoteInterface(long timeoutMillis,
                                                                       long pollIntervalMillis) throws Exception {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000;
        Exception lastException;
        do {
            try {
                return loadAliveRemoteInterface();
            } catch (Exception e) {
                lastException = e;
            }

            if (System.nanoTime() >= deadline) {
                break;
            }
            try {
                Thread.sleep(Math.clamp((deadline - System.nanoTime()) / 1_000_000, 1, pollIntervalMillis));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RemoteException("Interrupted while waiting for starter recovery", e);
            }
        } while (System.nanoTime() < deadline);

        throw new RemoteException("Starter recovery timed out before the RMI endpoint became available", lastException);
    }

    private static RemoteRestartCInterface loadAliveRemoteInterface() throws Exception {
        RemoteRestartCInterface remoteInterface = RemoteRegistryService.loadRemoteInterface();
        if (!remoteInterface.isAlive()) {
            throw new RemoteException("PacOS starter health check returned false");
        }
        return remoteInterface;
    }
}
