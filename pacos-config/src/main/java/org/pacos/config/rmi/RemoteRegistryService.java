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
        RemoteRegistryService.exportedInstance = remote;
        int registryPort = getRegistryPort();
        try {
            Remote stub = UnicastRemoteObject.exportObject(remote, 0); // 0 = random port (perfect locally)
            Registry registry = getRegistry(registryPort);
            registry.rebind(REGISTRY_NAME, stub);
            LOG.info("Registered stub '{}' with name '{}' in registry", exportedInstance, REGISTRY_NAME);
        }catch (Exception e) {
            LOG.error("Failed to register remote interface", e);
        }
    }

    private static Registry getRegistry(int registryPort) throws RemoteException {
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(registryPort);
            LOG.info("Created RMI registry on {}:{}", HOST, registryPort);
        } catch (ExportException e) {
            registry = LocateRegistry.getRegistry(HOST, registryPort);
            LOG.info("Using existing RMI registry on {}:{}", HOST, registryPort);
        }
        return registry;
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
