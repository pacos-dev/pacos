package org.pacos.core.component.plugin.manager.data;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.server.RequestHandler;
import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.listener.PluginListener;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.manager.PluginRequestHandler;
import org.pacos.core.component.plugin.service.PluginResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * This class contains objects connected with the plugin context
 */
public class PluginDataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginDataLoader.class);
    private final ApplicationContext context;

    private final Set<WindowConfig> windowConfigSet;
    private final Set<SettingTab> settingsTab;
    private final Set<VariableProvider> variableProviders;

    private Set<RequestHandler> requestHandlers;
    private Set<RequestHandlerRegistration> requestHandlerRegistration;
    private RequestMapping requestMapping;
    private PluginJar pluginJar;
    private Set<PluginListener> pluginListeners;

    public PluginDataLoader(ApplicationContext pluginContext,
            PluginJar pluginJar
    ) {
        this(pluginContext);
        this.pluginJar = pluginJar;
        this.requestMapping = loadRequestMapping(pluginContext);
        this.requestHandlers = loadRequestHandlers(pluginContext, pluginJar);
    }

    public PluginDataLoader(ApplicationContext pacosContext) {
        this.context = pacosContext;
        this.windowConfigSet = loadPluginWindows(pacosContext);
        this.settingsTab = loadSettingTabs(pacosContext);
        this.variableProviders = loadVariableProviders(pacosContext);
        this.pluginListeners = loadPluginListener(pacosContext);
    }

    private Set<PluginListener> loadPluginListener(ApplicationContext context) {
        Map<String, PluginListener> beans = context.getBeansOfType(PluginListener.class);
        return new HashSet<>(beans.values());
    }

    private RequestMapping loadRequestMapping(ApplicationContext context) {
        try {
            return new RequestMapping(context.getBean(RequestMappingInfoHandlerMapping.class)
                    , context.getBean(RequestMappingHandlerAdapter.class));
        } catch (NullPointerException e) {
            //eat this error. Plugin does not contain api
            return new RequestMapping(null, null);
        }
    }

    private Set<VariableProvider> loadVariableProviders(ApplicationContext context) {
        Map<String, VariableProvider> beans = context.getBeansOfType(VariableProvider.class);
        return new HashSet<>(beans.values());
    }

    private static HashSet<WindowConfig> loadPluginWindows(ApplicationContext pluginContext) {
        Map<String, WindowConfig> beans = pluginContext.getBeansOfType(WindowConfig.class);
        return new HashSet<>(beans.values());
    }

    private static Set<SettingTab> loadSettingTabs(ApplicationContext pluginContext) {
        return new HashSet<>(pluginContext.getBeansOfType(SettingTab.class).values());
    }

    private static Set<RequestHandler> loadRequestHandlers(ApplicationContext pluginContext, PluginJar pluginJar) {
        HashSet<RequestHandler> requestHandlers =
                new HashSet<>(pluginContext.getBeansOfType(RequestHandler.class).values());
        Set<String> resources = PluginResourceReader.readResourceList(pluginJar);
        if (requestHandlers.isEmpty() && !resources.isEmpty()) {
            try {
                requestHandlers.add(new PluginRequestHandler(resources, pluginJar.getPluginClassLoader()));
            } catch (MalformedURLException e) {
                LOG.error("Error while creating default request handler for plugin {}", pluginJar.getLibPath(), e);
            }
        }
        return requestHandlers;
    }

    public ApplicationContext context() {
        return context;
    }

    public void setRequestHandlerRegistration(Set<RequestHandlerRegistration> requestHandlerRegistration) {
        this.requestHandlerRegistration = requestHandlerRegistration;
    }

    public Set<RequestHandlerRegistration> getRequestHandlerRegistration() {
        return requestHandlerRegistration;
    }

    public Set<SettingTab> getSettingsTab() {
        return settingsTab;
    }

    public Set<WindowConfig> getWindowConfigSet() {
        return windowConfigSet;
    }

    public Set<VariableProvider> getVariableProviders() {
        return variableProviders;
    }

    public Set<RequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

    public RequestMapping getRequestMapping() {
        return requestMapping;
    }

    public Set<PluginListener> getPluginListeners() {
        return pluginListeners;
    }

    public void close() {
        pluginJar.closeClassLoader();
        if (context() != null && context() instanceof ConfigurableApplicationContext configurableContext) {
            configurableContext.close();
        }
    }
}
