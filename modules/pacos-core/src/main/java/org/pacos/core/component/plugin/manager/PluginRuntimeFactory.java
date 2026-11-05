package org.pacos.core.component.plugin.manager;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Creates the isolated Spring application context used by a plugin runtime.
 */
@Component
public class PluginRuntimeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PluginRuntimeFactory.class);

    private final ApplicationContext coreContext;

    public PluginRuntimeFactory(ApplicationContext coreContext) {
        this.coreContext = coreContext;
    }

    public ConfigurableApplicationContext createContext(PluginJar pluginJar, String pluginName)
            throws MalformedURLException {
        Instant start = Instant.now();
        ConfigurableApplicationContext context =
                loadModuleContext(coreContext, pluginJar.getPluginClassLoader(), pluginName);
        Duration timeTaken = Duration.between(start, Instant.now());
        LOG.info("Plugin initialization took {} ms", timeTaken.toMillis());
        return context;
    }

    private static ConfigurableApplicationContext loadModuleContext(ApplicationContext parentContext,
                                                                    ClassLoader moduleClassLoader, String pluginName) {
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
                safeExecution(moduleContext::close, e::addSuppressed);
                throw e;
            }
        } finally {
            moduleLogger.stopLogger();
        }
    }

    private static boolean safeExecution(Runnable action, Consumer<Exception> onFailure) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            onFailure.accept(e);
            return false;
        }
    }
}
