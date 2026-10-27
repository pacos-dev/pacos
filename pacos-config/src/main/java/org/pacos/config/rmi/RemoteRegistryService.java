package org.pacos.config.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.pacos.config.property.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoteRegistryService {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteRegistryService.class);

    private static final String HOST = LoopbackRMIServerSocketFactory.HOST;
    private static final int DEFAULT_REGISTRY_PORT = 1099;
    private static final String REGISTRY_NAME = "remoteRestartInterface";

    private static final Map<Integer, Registry> OWNED_REGISTRIES = new HashMap<>();
    private static final Map<Integer, Remote> REGISTERED_REMOTE_OBJECTS = new HashMap<>();

    private RemoteRegistryService() {
    }

    public static synchronized void registerRemoteInterface(Remote remote) {
        int registryPort = -1;
        Registry registry = null;
        boolean registryCreatedHere = false;
        boolean remoteExportedHere = false;

        try {
            System.setProperty("java.rmi.server.hostname", HOST);
            registryPort = getRegistryPort();

            registry = OWNED_REGISTRIES.get(registryPort);
            if (registry == null) {
                // Bind the registry socket to loopback instead of every network interface.
                // Do not fall back to LocateRegistry.getRegistry() if the port is occupied:
                // that registry may belong to another application or PacOS instance.
                registry = LocateRegistry.createRegistry(
                        registryPort, null, LoopbackRMIServerSocketFactory.INSTANCE);
                OWNED_REGISTRIES.put(registryPort, registry);
                registryCreatedHere = true;
                LOG.info("Created loopback RMI registry on {}:{}", HOST, registryPort);
            }

            RemoteExport remoteExport = exportOrReuse(remote);
            Remote stub = remoteExport.stub();
            remoteExportedHere = remoteExport.exportedHere();

            registry.rebind(REGISTRY_NAME, stub);
            // RMI registries keep only the stub. Retain the implementation strongly so
            // it cannot be garbage-collected while the registry still publishes that stub.
            REGISTERED_REMOTE_OBJECTS.put(registryPort, remote);
            LOG.info("Registered remote restart interface on {}:{}", HOST, registryPort);
        } catch (RemoteException | RuntimeException e) {
            if (remoteExportedHere) {
                unexportAfterFailedRegistration(remote, "remote restart interface");
            }
            if (registryCreatedHere) {
                OWNED_REGISTRIES.remove(registryPort, registry);
                unexportAfterFailedRegistration(registry, "RMI registry");
            }

            // Restart over RMI is an optional management capability. A failure to publish it
            // must not prevent the main PacOS application from starting.
            LOG.warn("RMI restart interface could not be registered. PacOS will continue to start, "
                    + "but remote restart will be unavailable.", e);
        }
    }

    public static RemoteRestartCInterface loadRemoteInterface() throws RemoteException, NotBoundException {
        int registryPort = getRegistryPort();
        Registry registry = LocateRegistry.getRegistry(HOST, registryPort);
        return (RemoteRestartCInterface) registry.lookup(REGISTRY_NAME);
    }

    private static int getRegistryPort() {
        String rmiPort = System.getProperty(PropertyName.RMI_PORT.getPropertyName());
        if (rmiPort == null) {
            return DEFAULT_REGISTRY_PORT;
        }

        int port = Integer.parseInt(rmiPort.trim());
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("RMI registry port must be between 1 and 65535");
        }
        return port;
    }

    /**
     * The remote object endpoint must also be loopback-bound. Setting
     * java.rmi.server.hostname only controls the address advertised in stubs;
     * it does not restrict the server socket's bind address.
     */
    private static RemoteExport exportOrReuse(Remote remote) throws RemoteException {
        try {
            Remote stub = UnicastRemoteObject.exportObject(
                    remote, 0, null, LoopbackRMIServerSocketFactory.INSTANCE);
            return new RemoteExport(stub, true);
        } catch (ExportException alreadyExported) {
            // Re-registering an already exported object is safe; reuse its existing stub.
            return new RemoteExport(RemoteObject.toStub(remote), false);
        }
    }

    private static void unexportAfterFailedRegistration(Remote remote, String resourceName) {
        try {
            UnicastRemoteObject.unexportObject(remote, true);
        } catch (NoSuchObjectException e) {
            LOG.debug("RMI {} was not exported when cleanup was attempted", resourceName, e);
        }
    }

    private record RemoteExport(Remote stub, boolean exportedHere) {
    }
}