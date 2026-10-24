package org.pacos.config.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import org.pacos.config.property.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoteRegistryService {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteRegistryService.class);

    private static final String HOST = "127.0.0.1";
    private static final int DEFAULT_REGISTRY_PORT = 1099;
    private static final String REGISTRY_NAME = "remoteRestartInterface";
    private static Remote exportedInstance;

    private RemoteRegistryService() {
    }

    public static void registerRemoteInterface(Remote remote) {
        System.setProperty("java.rmi.server.hostname", HOST);
        int registryPort = getRegistryPort();
        try {
            Remote stub;
            try {
                stub = UnicastRemoteObject.exportObject(remote, 0);
            } catch (ExportException alreadyExported) {
                // Re-registering an already exported object is safe; reuse its existing stub.
                stub = UnicastRemoteObject.toStub(remote);
            }

            Registry registry = getRegistry(registryPort);
            registry.rebind(REGISTRY_NAME, stub);
            exportedInstance = remote;
            LOG.info("Registered stub '{}' with name '{}' in registry", exportedInstance, REGISTRY_NAME);
        } catch (RemoteException e) {
            // Failing to publish the registry entry must fail startup, rather than leaving an apparently
            // running PacOS instance whose restart control interface is unavailable.
            throw new IllegalStateException("Failed to register remote interface in the RMI registry", e);
        }
    }

    private static Registry getRegistry(int registryPort) throws RemoteException {
        try {
            Registry registry = LocateRegistry.createRegistry(registryPort);
            LOG.info("Created RMI registry on {}:{}", HOST, registryPort);
            return registry;
        } catch (ExportException e) {
            Registry registry = LocateRegistry.getRegistry(HOST, registryPort);
            LOG.info("Using existing RMI registry on {}:{}", HOST, registryPort);
            return registry;
        }
    }

    public static RemoteRestartCInterface loadRemoteInterface() throws RemoteException, NotBoundException {
        int registryPort = getRegistryPort();
        Registry registry = LocateRegistry.getRegistry(HOST, registryPort);
        return (RemoteRestartCInterface) registry.lookup(REGISTRY_NAME);
    }

    private static int getRegistryPort() {
        String rmiPort = System.getProperty(PropertyName.RMI_PORT.getPropertyName());
        return rmiPort != null ? Integer.parseInt(rmiPort) : DEFAULT_REGISTRY_PORT;
    }
}
