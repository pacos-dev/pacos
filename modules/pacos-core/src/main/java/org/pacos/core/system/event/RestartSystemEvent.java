package org.pacos.core.system.event;

import java.rmi.NotBoundException;
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
                                                                     long pollIntervalMillis) throws RemoteException {
        try {
            return loadAliveRemoteInterface();
        } catch (RemoteException initialFailure) {
            LOG.warn("PacOS starter health check failed; trying to recover it in attach mode", initialFailure);
            RemoteRestartCInterface attached = tryAttachRecovery(attachTimeoutMillis, pollIntervalMillis);
            if (attached != null) {
                return attached;
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new RemoteException("Interrupted while recovering the PacOS starter", initialFailure);
            }
            return recoverByStartingNewStarter(fallbackTimeoutMillis, pollIntervalMillis, initialFailure);
        }
    }

    private static RemoteRestartCInterface tryAttachRecovery(long timeoutMillis,
                                                              long pollIntervalMillis) throws RemoteException {
        if (!StarterRecoveryService.startInAttachMode()) {
            return null;
        }
        try {
            return waitForAliveRemoteInterface(timeoutMillis, pollIntervalMillis);
        } catch (RemoteException attachFailure) {
            if (Thread.currentThread().isInterrupted()) {
                throw attachFailure;
            }
            LOG.warn("Starter attach recovery did not restore RMI", attachFailure);
            return null;
        }
    }

    private static RemoteRestartCInterface recoverByStartingNewStarter(long timeoutMillis,
                                                                        long pollIntervalMillis,
                                                                        RemoteException attachFailure)
            throws RemoteException {
        LOG.warn("RMI recovery by attach failed; starting the starter in recovery mode");
        if (!StarterRecoveryService.startInRecoveryMode()) {
            throw new RemoteException("Could not start PacOS starter in recovery mode", attachFailure);
        }
        try {
            return waitForAliveRemoteInterface(timeoutMillis, pollIntervalMillis);
        } catch (RemoteException fallbackFailure) {
            fallbackFailure.addSuppressed(attachFailure);
            throw fallbackFailure;
        }
    }

    private static RemoteRestartCInterface waitForAliveRemoteInterface(long timeoutMillis,
                                                                       long pollIntervalMillis) throws RemoteException {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000;
        RemoteException lastFailure = null;
        do {
            try {
                return loadAliveRemoteInterface();
            } catch (RemoteException e) {
                lastFailure = e;
            }

            if (System.nanoTime() >= deadline) {
                break;
            }
            try {
                long remainingMillis = Math.max(1, (deadline - System.nanoTime()) / 1_000_000);
                Thread.sleep(Math.min(pollIntervalMillis, remainingMillis));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RemoteException("Interrupted while waiting for starter recovery", e);
            }
        } while (System.nanoTime() < deadline);

        throw new RemoteException("Starter recovery timed out before the RMI endpoint became available",
                lastFailure);
    }

    private static RemoteRestartCInterface loadAliveRemoteInterface() throws RemoteException {
        try {
            RemoteRestartCInterface remoteInterface = RemoteRegistryService.loadRemoteInterface();
            if (remoteInterface == null || !remoteInterface.isAlive()) {
                throw new RemoteException("PacOS starter health check returned false");
            }
            return remoteInterface;
        } catch (NotBoundException e) {
            throw new RemoteException("PacOS starter restart endpoint is not bound in the RMI registry", e);
        } catch (RuntimeException e) {
            throw new RemoteException("PacOS starter restart endpoint could not be queried", e);
        }
    }
}
