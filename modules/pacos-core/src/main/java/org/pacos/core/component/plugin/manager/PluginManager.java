package org.pacos.core.component.plugin.manager;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.vaadin.flow.server.RequestHandler;
import org.pacos.base.event.ModuleEvent;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.pacos.core.component.plugin.manager.data.PluginStatus;
import org.pacos.core.component.plugin.manager.data.RequestHandlerRegistration;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.session.service.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Responsible for managing the plugin life cycle
 */
@Component
public class PluginManager {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManager.class);

    private PluginResource pluginResource;
    private final ApplicationContext coreContext;
    private final PluginService pluginService;
    private final SwaggerUIConfigReload swaggerUIConfigReload;
    private final PluginState pluginState;
    private final ConcurrentMap<PluginKey, LifecycleLock> lifecycleLocks = new ConcurrentHashMap<>();

    public PluginManager(PluginService pluginService, SwaggerUIConfigReload swaggerUIConfigReload,
            ApplicationContext coreContext, PluginState pluginState) {
        this.coreContext = coreContext;
        this.pluginService = pluginService;

        this.swaggerUIConfigReload = swaggerUIConfigReload;
        this.pluginState = pluginState;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializePluginsOnApplicationReadyEvent() {
        pluginResource = new PluginResource(coreContext);
        pluginService.findNotRemovedPlugin().forEach(this::addPlugin);
        pluginService.findEnabledPlugin().forEach(this::startPlugin);
    }

    /**
     * Add state information about plugins during startup and after installation
     */
    public void addPlugin(PluginDTO plugin) {
        pluginState.addPlugin(plugin);
    }

    /**
     * Remove plugin state and resources during update and manual uninstall
     */
    public void removePlugin(PluginDTO pluginDTO) {
        PluginKey key = PluginKey.from(pluginDTO);
        LifecycleLock lock = acquireLifecycleLock(key);
        lock.lock.lock();
        try {
            removePluginLocked(pluginDTO);
        } finally {
            releaseLifecycleLock(key, lock);
        }
    }

    private void removePluginLocked(PluginDTO pluginDTO) {
        PluginStatusEnum state = pluginState.getState(pluginDTO);
        if (state != null && (state.isOn() || state.isInitialized())) {
            PluginDataLoader pluginData = pluginResource.get(pluginDTO);
            try {
                pluginResource.remove(pluginDTO);
                if (pluginData != null) {
                    removePluginExtensionsFromPacos(pluginData);
                }
            } finally {
                try {
                    if (pluginData != null) {
                        pluginData.close();
                    }
                } finally {
                    pluginState.removePlugin(pluginDTO);
                }
            }
            return;
        }
        pluginState.removePlugin(pluginDTO);
    }

    /**
     * Stop plugin (if started), removes all resources and plugin context
     */
    @Async("pluginContextExecutor")
    public CompletableFuture<Boolean> stopPlugin(PluginDTO plugin) {
        PluginKey key = PluginKey.from(plugin);
        LifecycleLock lock = acquireLifecycleLock(key);
        lock.lock.lock();
        try {
            return stopPluginLocked(plugin);
        } finally {
            releaseLifecycleLock(key, lock);
        }
    }

    private LifecycleLock acquireLifecycleLock(PluginKey key) {
        return lifecycleLocks.compute(key, (ignored, current) -> {
            LifecycleLock selected = current == null ? new LifecycleLock() : current;
            selected.references++;
            return selected;
        });
    }

    private void releaseLifecycleLock(PluginKey key, LifecycleLock lock) {
        lock.lock.unlock();
        lifecycleLocks.computeIfPresent(key, (ignored, current) -> {
            current.references--;
            return current.references == 0 ? null : current;
        });
    }

    private static final class LifecycleLock {
        private final ReentrantLock lock = new ReentrantLock();
        private int references;
    }

    private record PluginKey(String groupId, String artifactName, String version) {
        private static PluginKey from(PluginDTO plugin) {
            return new PluginKey(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        }
    }

    private CompletableFuture<Boolean> stopPluginLocked(PluginDTO plugin) {
        if (!pluginState.canStop(plugin)) {
            return CompletableFuture.completedFuture(true);
        }
        LOG.info("Stopping plugin {}", plugin);
        PluginDataLoader pluginData = pluginResource.get(plugin);
        changePluginStatus(plugin, PluginStatusEnum.SHUTDOWN);
        if (pluginData == null) {
            changePluginStatus(plugin, PluginStatusEnum.OFF);
            swaggerUIConfigReload.removeConfiguration(plugin);
            return CompletableFuture.completedFuture(true);
        }

        boolean stopped = true;
        try {
            pluginData.getWindowConfigSet().forEach(windowConfig ->
                    ServiceListener.notifyAll(ModuleEvent.MODULE_REMOVED, windowConfig));
        } catch (Exception e) {
            stopped = false;
            LOG.error("Failed to notify windows about plugin shutdown: {}", plugin, e);
        }
        try {
            removePluginExtensionsFromPacos(pluginData);
        } catch (Exception e) {
            stopped = false;
            LOG.error("Failed to unregister plugin extensions: {}", plugin, e);
        } finally {
            try {
                pluginResource.remove(plugin);
            } catch (Exception e) {
                stopped = false;
                LOG.error("Failed to remove plugin resources: {}", plugin, e);
            }
            try {
                pluginData.close();
            } catch (Exception e) {
                stopped = false;
                LOG.error("Failed to close plugin context: {}", plugin, e);
            }
            changePluginStatus(plugin, PluginStatusEnum.OFF);
            swaggerUIConfigReload.removeConfiguration(plugin);
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_UNINSTALLED, plugin);
        }

        LOG.info("Plugin {} stopped", plugin);
        return CompletableFuture.completedFuture(stopped);
    }

    /**
     * Initialize given plugin
     * The spring context and all resources will be loaded from jar file assigned to this plugin
     */
    @Async("pluginContextExecutor")
    public CompletableFuture<Boolean> startPlugin(PluginDTO plugin) {
        PluginKey key = PluginKey.from(plugin);
        LifecycleLock lock = acquireLifecycleLock(key);
        lock.lock.lock();
        try {
            return startPluginLocked(plugin);
        } finally {
            releaseLifecycleLock(key, lock);
        }
    }

    private CompletableFuture<Boolean> startPluginLocked(PluginDTO plugin) {
        PluginJar jarPath = null;
        PluginDataLoader pluginData = null;
        try {
            if (!pluginState.canRun(plugin)) {
                return CompletableFuture.completedFuture(true);
            }
            LOG.info("Initializing plugin {}", plugin.getName());
            changePluginStatus(plugin, PluginStatusEnum.INITIALIZATION);
            jarPath = new PluginJar(plugin);
            if (!jarPath.exists()) {
                LOG.error("Can't find jar file {}", jarPath);
                jarPath.closeClassLoader();
                changePluginStatus(plugin, PluginStatusEnum.ERROR);
                return CompletableFuture.completedFuture(false);
            }
            pluginData = initializePluginContext(plugin, jarPath);
            addPluginExtensionsToPacos(pluginData);

            changePluginStatus(plugin, PluginStatusEnum.ON);
            swaggerUIConfigReload.addConfiguration(plugin);
            ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALLED, plugin);
            LOG.info("Plugin {} started", plugin);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            if (pluginData != null) {
                try {
                    removePluginExtensionsFromPacos(pluginData);
                } catch (Exception cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                try {
                    pluginResource.remove(plugin);
                } catch (Exception cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                try {
                    pluginData.close();
                } catch (Exception cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            } else if (jarPath != null) {
                jarPath.closeClassLoader();
            }
            changePluginStatus(plugin, PluginStatusEnum.ERROR);
            LOG.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private PluginDataLoader initializePluginContext(PluginDTO plugin, PluginJar pluginJar)
            throws MalformedURLException {
        Instant start = Instant.now();
        ApplicationContext pluginContext =
                loadModuleContext(coreContext, pluginJar.getPluginClassLoader(), plugin.getArtifactName());
        Duration timeTaken = Duration.between(start, Instant.now());
        LOG.info("Plugin initialization took {} ms", timeTaken.toMillis());

        try {
            return pluginResource.add(plugin, pluginContext, pluginJar);
        } catch (RuntimeException e) {
            try {
                if (pluginContext instanceof org.springframework.context.ConfigurableApplicationContext configurableContext) {
                    configurableContext.close();
                }
            } catch (RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            } finally {
                pluginJar.closeClassLoader();
            }
            throw e;
        }
    }

    private static ApplicationContext loadModuleContext(ApplicationContext parentContext, ClassLoader moduleClassLoader,
            String pluginName) {
        ModuleLogger moduleLogger = new ModuleLogger(pluginName);
        try {
            moduleLogger.getLogger().info("Starting module initialization: {}", pluginName);
            AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
            try {
                moduleContext.setClassLoader(moduleClassLoader);
                moduleContext.setParent(parentContext);
                moduleContext.scan("org.pacos.plugin." + pluginName + ".config");
                moduleContext.registerBean(RequestMappingHandlerMapping.class);
                moduleContext.refresh();
                moduleLogger.getLogger().info("Package scanning set to org.pacos.plugin.{}.config", pluginName);
                moduleLogger.getLogger().info("Module initialized successfully: {}", pluginName);
                return moduleContext;
            } catch (RuntimeException e) {
                try {
                    moduleContext.close();
                } catch (RuntimeException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                throw e;
            }
        } finally {
            moduleLogger.stopLogger();
        }
    }

    private void changePluginStatus(PluginDTO plugin, PluginStatusEnum pluginStateEnum) {
        pluginState.setState(plugin, pluginStateEnum);
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED, new PluginStatus(plugin, pluginStateEnum));
    }

    private void removePluginExtensionsFromPacos(PluginDataLoader pluginData) {
        RuntimeException cleanupFailure = null;
        for (RequestHandlerRegistration handler : pluginData.getRequestHandlerRegistration()) {
            try {
                ServiceListener.removeRequestHandler(handler.resourceHandler());
            } catch (RuntimeException e) {
                cleanupFailure = collectCleanupFailure(cleanupFailure, e);
            }
            try {
                handler.registration().remove();
            } catch (RuntimeException e) {
                cleanupFailure = collectCleanupFailure(cleanupFailure, e);
            }
        }
        try {
            ServiceListener.removeVariableProviders(pluginData.getVariableProviders());
        } catch (RuntimeException e) {
            cleanupFailure = collectCleanupFailure(cleanupFailure, e);
        }
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
    }

    private static RuntimeException collectCleanupFailure(RuntimeException current, RuntimeException next) {
        if (current == null) {
            return next;
        }
        current.addSuppressed(next);
        return current;
    }

    private void addPluginExtensionsToPacos(PluginDataLoader pluginData) {
        for (RequestHandler handler : pluginData.getRequestHandlers()) {
            pluginData.addRequestHandlerRegistration(
                    new RequestHandlerRegistration(ServiceListener.addRequestHandler(handler), handler));
        }
        ServiceListener.addVariableProviders(pluginData.getVariableProviders());
    }

}
