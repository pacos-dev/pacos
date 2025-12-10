package org.pacos.core.component.plugin.manager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.component.setting.SettingTab;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.FileOpenAllowed;
import org.pacos.base.window.config.FileExtensionHandler;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginDataLoader;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.pacos.core.component.plugin.manager.data.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * It allows static access to plugin resources such as window configuration, settings
 * It is initialized on spring context ready event
 */
public class PluginResource {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResource.class);

    private final Map<PluginDTO, PluginDataLoader> pluginDataMap = new HashMap<>();
    private final PluginDataLoader coreData;
    private static PluginResource pluginResource;

    public PluginResource(ApplicationContext coreContext) {
        coreData = new PluginDataLoader(coreContext);
        pluginResource = this;
    }

    public PluginDataLoader get(PluginDTO plugin) {
        return pluginDataMap.get(plugin);
    }

    public void remove(PluginDTO plugin) {
        //Inform all plugins about removed plugin
        pluginDataMap.values()
                .forEach(e -> e.getPluginListeners()
                        .forEach(l -> {
                            try {
                                l.pluginRemoved(pluginDataMap.get(plugin).context());
                            } catch (Exception ex) {
                                LOG.error("Error while inform existing plugins about removed plugin context", ex);
                            }
                        }));
        //Inform core listeners about removed plugin
        coreData.getPluginListeners().forEach(l ->
                {
                    try {
                        l.pluginRemoved(pluginDataMap.get(plugin).context());
                    } catch (Exception e) {
                        LOG.error("Error while inform core about removed plugin context", e);
                    }
                }
        );
        pluginDataMap.remove(plugin);
    }

    public PluginDataLoader add(PluginDTO plugin, ApplicationContext pluginContext,
            PluginJar pluginJar) {
        PluginDataLoader pluginData = new PluginDataLoader(pluginContext, pluginJar);
        //Inform all already installed plugins about new plugin context
        pluginDataMap.values().forEach(e -> e.getPluginListeners().forEach(l -> {
            try {
                l.pluginInitialized(pluginContext);
            } catch (Exception ex) {
                LOG.error("Error while inform existing plugins about new plugin context", ex);
            }
        }));
        //Inform new plugin context about already installed plugins
        pluginData.getPluginListeners().forEach(l ->
                pluginDataMap.values().forEach(installedPlugin -> {
                    try {
                        l.pluginInitialized(installedPlugin.context());
                    } catch (Exception ex) {
                        LOG.error("Error while inform currently installed plugin about existing plugin context", ex);
                    }
                }));
        //Inform core listeners about new context plugin
        coreData.getPluginListeners().forEach(l -> {
            try {
                l.pluginInitialized(pluginContext);
            } catch (Exception ex) {
                LOG.error("Error while inform currently installed plugin about existing plugin context", ex);
            }
        });
        pluginDataMap.put(plugin, pluginData);
        return pluginData;
    }

    public static Set<SettingTab> loadAvailableSettingTabs(UserSession session) {
        return getAllData().stream().map(PluginDataLoader::getSettingsTab)
                .flatMap(Set::stream)
                .filter(settingTab -> {
                    try {
                        return settingTab.shouldBeDisplayed(session);
                    } catch (Exception e) {
                        LOG.error("Failed to load setting tab {}", e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

    public static Set<WindowConfig> getAllWindowConfig() {
        return getAllData().stream().map(PluginDataLoader::getWindowConfigSet)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    public static Set<VariableProvider> getAllVariableProvider() {
        return getAllData().stream().map(PluginDataLoader::getVariableProviders)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    public static Set<WindowConfig> getAppWindowConfigForUser(UserSession userSession) {
        Set<WindowConfig> configs = getAllWindowConfig();
        return configs
                .stream().filter(e -> e.isApplication() && e.isAllowedForCurrentSession(userSession))
                .collect(Collectors.toSet());
    }

    public static WindowConfig findWindowAllowedForExtension(String extension, UserSession userSession) {
        return getAllWindowConfigForUser(userSession).stream()
                .filter(e -> Arrays.asList(e.activatorClass().getInterfaces()).contains(FileOpenAllowed.class))
                .filter(FileExtensionHandler.class::isInstance)
                .filter(e -> ((FileExtensionHandler) e).allowedExtension().contains(extension.toLowerCase()))
                .findFirst().orElse(null);
    }

    public static ApplicationContext getModuleContextForWindowConfig(Class<?> clazz) {
        return loadContextForWindowConfig(clazz);
    }

    public static ApplicationContext loadContextForWindowConfig(Class<?> clazz) {
        for (PluginDataLoader windowConfig : getAllData()) {
            for (WindowConfig config : windowConfig.getWindowConfigSet()) {
                if (config.getClass().equals(clazz)) {
                    return windowConfig.context();
                }
            }
        }
        return null;
    }

    public static Optional<RequestMapping> loadRequestMappingForPluginName(String pluginName) {
        return pluginResource.pluginDataMap.entrySet()
                .stream()
                .filter(e -> e.getKey().getArtifactName().equals(pluginName))
                .map(e -> e.getValue().getRequestMapping())
                .findFirst();
    }

    /**
     * Combine plugin data with core data
     */
    private static Set<PluginDataLoader> getAllData() {
        if (pluginResource == null) {
            //It may occur during server restart
            throw new IllegalStateException("The page was reloaded by browser before context initialized. "
                    + "Please refresh the page");
        }
        Set<PluginDataLoader> dataSet = new HashSet<>(pluginResource.pluginDataMap.values());
        dataSet.add(pluginResource.coreData);
        return dataSet;
    }

    /**
     * Return all window config including no application config.
     */
    private static Set<WindowConfig> getAllWindowConfigForUser(UserSession userSession) {
        Set<WindowConfig> configs = getAllWindowConfig();
        return configs
                .stream().filter(e -> e.isAllowedForCurrentSession(userSession))
                .collect(Collectors.toSet());
    }
}
