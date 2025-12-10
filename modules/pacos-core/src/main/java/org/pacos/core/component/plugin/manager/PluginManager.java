package org.pacos.core.component.plugin.manager;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public PluginManager(PluginService pluginService, SwaggerUIConfigReload swaggerUIConfigReload,
            ApplicationContext coreContext) {
        this.coreContext = coreContext;
        this.pluginService = pluginService;

        this.swaggerUIConfigReload = swaggerUIConfigReload;
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
        PluginState.addPlugin(plugin);
    }

    /**
     * Remove plugin state and resources during update and manual uninstall
     */
    public void removePlugin(PluginDTO pluginDTO) {
        PluginStatusEnum state = PluginState.getState(pluginDTO);
        if (PluginState.removePlugin(pluginDTO) != null && (state.isOn() || state.isInitialized())) {
            pluginResource.remove(pluginDTO);
        }
    }

    /**
     * Stop plugin (if started), removes all resources and plugin context
     */
    @Async("pluginContextExecutor")
    public CompletableFuture<Boolean> stopPlugin(PluginDTO plugin) {
        if (!PluginState.canStop(plugin)) {
            return CompletableFuture.completedFuture(true);
        }
        LOG.info("Stopping plugin {}", plugin);
        PluginDataLoader pluginData = pluginResource.get(plugin);
        changePluginStatus(plugin, PluginStatusEnum.SHUTDOWN);
        //close all existing instances of window created based on plugin
        pluginData.getWindowConfigSet().forEach(windowConfig ->
                ServiceListener.notifyAll(ModuleEvent.MODULE_REMOVED, windowConfig));
        pluginResource.remove(plugin);

        removePluginExtensionsFromPacos(pluginData);
        pluginData.close();

        changePluginStatus(plugin, PluginStatusEnum.OFF);
        swaggerUIConfigReload.removeConfiguration(plugin);
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_UNINSTALLED, plugin);

        LOG.info("Plugin {} stopped", plugin);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Initialize given plugin
     * The spring context and all resources will be loaded from jar file assigned to this plugin
     */
    @Async("pluginContextExecutor")
    public CompletableFuture<Boolean> startPlugin(PluginDTO plugin) {
        PluginJar jarPath = null;
        PluginDataLoader pluginData = null;
        try {
            if (!PluginState.canRun(plugin)) {
                return CompletableFuture.completedFuture(true);
            }
            LOG.info("Initializing plugin {}", plugin.getName());
            changePluginStatus(plugin, PluginStatusEnum.INITIALIZATION);
            jarPath = new PluginJar(plugin);
            if (!jarPath.exists()) {
                LOG.error("Can't find jar file {}", jarPath);
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
                pluginData.close();
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

        return pluginResource.add(plugin, pluginContext, pluginJar);
    }

    private static ApplicationContext loadModuleContext(ApplicationContext parentContext, ClassLoader moduleClassLoader,
            String pluginName) {
        ModuleLogger moduleLogger = new ModuleLogger(pluginName);
        try {
            moduleLogger.getLogger().info("Starting module initialization: {}", pluginName);
            AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
            moduleContext.setClassLoader(moduleClassLoader);
            moduleContext.setParent(parentContext);
            moduleContext.scan("org.pacos.plugin." + pluginName + ".config");
            //Register RequestMappingHandler to enable API
            moduleContext.registerBean(RequestMappingHandlerMapping.class);
            moduleContext.refresh();
            moduleLogger.getLogger().info("Package scanning set to org.pacos.plugin.{}.config", pluginName);
            moduleLogger.getLogger().info("Module initialized successfully: {}", pluginName);
            return moduleContext;
        } finally {
            moduleLogger.stopLogger();
        }
    }

    private static void changePluginStatus(PluginDTO plugin, PluginStatusEnum pluginStateEnum) {
        PluginState.setState(plugin, pluginStateEnum);
        ServiceListener.notifyAll(ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED, new PluginStatus(plugin, pluginStateEnum));
    }

    private void removePluginExtensionsFromPacos(PluginDataLoader pluginData) {
        pluginData.getRequestHandlerRegistration().forEach(handler -> {
            ServiceListener.removeRequestHandler(handler.resourceHandler());
            handler.registration().remove();
        });
        ServiceListener.removeVariableProviders(pluginData.getVariableProviders());
    }

    private void addPluginExtensionsToPacos(PluginDataLoader pluginData) {
        Collection<RequestHandler> requestHandlers = pluginData.getRequestHandlers();

        Set<RequestHandlerRegistration> registrations = requestHandlers.stream()
                .map(handler -> new RequestHandlerRegistration(ServiceListener.addRequestHandler(handler), handler))
                .collect(Collectors.toSet());
        pluginData.setRequestHandlerRegistration(registrations);

        ServiceListener.addVariableProviders(pluginData.getVariableProviders());
    }

}
