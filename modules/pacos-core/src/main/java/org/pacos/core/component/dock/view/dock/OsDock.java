package org.pacos.core.component.dock.view.dock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dnd.DropTarget;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates dock element on the bottom of the pacos primary desktop view
 * The Dock is personalized and displays icons of applications pinned by the user
 * When a module is launched, its icon appears in the dock and is removed when the application is closed.
 * Only icons pinned by the user are always displayed
 */
@Tag("ul")
public class OsDock extends HtmlContainer implements DropTarget<HtmlContainer> {

    private static final Logger LOG = LoggerFactory.getLogger(OsDock.class);
    private final Map<WindowConfig, OsDockElement> dockElementMap = new HashMap<>();
    private final transient DockServiceProxy dockServiceProxy;
    private final UserSession userSession;

    public OsDock(DockServiceProxy dockServiceProxy) {
        this.dockServiceProxy = dockServiceProxy;
        this.userSession = UserSession.getCurrent();
        setClassName("osx-dock");
        setId("osx-dock");
        addActivatorsForActiveModule();
        //reconfigure doc element when hotswapping session
        UISystem.getCurrent().getWindowManager().getAllInitializedWindows().forEach(this::activateDockElement);

        UISystem.getCurrent()
                .subscribeOnAttached(this, ModuleEvent.MODULE_OPENED, e -> activateDockElement((DesktopWindow) e));
        UISystem.getCurrent()
                .subscribeOnAttached(this, ModuleEvent.MODULE_SHUTDOWN, e -> deactivateDockElement((DesktopWindow) e));
        UISystem.getCurrent()
                .subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALLED, e -> addWindowToDockIfPluginInstalled());
        UISystem.getCurrent()
                .subscribeOnAttached(this, ModuleEvent.PLUGIN_UNINSTALLED, e -> removeDockElementIfPluginUninstalled());
    }

    private void removeDockElementIfPluginUninstalled() {
        Set<WindowConfig> availableModules = PluginResource.getAppWindowConfigForUser(userSession);
        dockElementMap.keySet().forEach(windowConfig -> {
            if (!availableModules.contains(windowConfig)) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    removeDockElement(windowConfig, true);
                    ui.push();
                }));
            }
        });
    }

    private void addWindowToDockIfPluginInstalled() {
        Set<WindowConfig> availableModules = PluginResource.getAppWindowConfigForUser(userSession);
        List<DockConfigurationDTO> userDockConfig = dockServiceProxy.loadConfigurations(userSession.getUserId());

        for (DockConfigurationDTO dockConfig : userDockConfig) {
            availableModules.stream().filter(config -> idDockConfigMath(config, dockConfig)).findFirst()
                    .ifPresent(config -> {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            if (!dockElementMap.containsKey(config)) {
                                addWindowIconToDock(config, dockConfig);
                            }
                            ui.push();
                        }));

                    });
        }
    }

    /**
     * Adds all user-configured items that are available to the user in a given session
     */
    private void addActivatorsForActiveModule() {
        List<DockConfigurationDTO> userDockConfig =
                dockServiceProxy.loadConfigurations(UserSession.getCurrent().getUserId());
        Set<WindowConfig> availableModules = PluginResource.getAppWindowConfigForUser(userSession);
        for (DockConfigurationDTO dockConfig : userDockConfig) {
            availableModules.stream().filter(config -> idDockConfigMath(config, dockConfig)).findFirst()
                    .ifPresent(config -> addWindowIconToDock(config, dockConfig));
        }
    }

    /**
     * Checks if given dock configuration can be assigned to allowed window
     */
    private boolean idDockConfigMath(WindowConfig windowConfig, DockConfigurationDTO dockConfig) {
        if (windowConfig.activatorClass() == null || dockConfig.getActivator() == null) {
            LOG.warn("Activator class is null. Skipping activation of {}", windowConfig);
            return false;
        }
        String activatorName = windowConfig.activatorClass().getSimpleName();
        return activatorName.equals(dockConfig.getActivator());

    }

    private synchronized OsDockElement addWindowIconToDock(WindowConfig config, DockConfigurationDTO dockConfig) {
        final OsDockElement osDockElement = new OsDockElement(this, config, dockConfig, dockServiceProxy);
        add(osDockElement);
        dockElementMap.put(config, osDockElement);
        return osDockElement;
    }

    /**
     * Triggered when user open a new window
     * Set css class "active" on dock element.
     * If activator for given window does not exist, then also add new element to the dock
     */
    void activateDockElement(DesktopWindow e) {
        if (e == null) {
            return;
        }
        if (dockElementMap.get(e.getConfig()) == null) {
            OsDockElement element = addWindowIconToDock(e.getConfig(), null);
            element.setActiveInstance(e);
        }
        dockElementMap.get(e.getConfig()).addClassName("active");
    }

    /**
     * Triggered when user close a window
     * Removes css class "active" from activator assigned to given window
     * If activator for given window is not part of a user configuration, then element is also removed from dock
     */
    void deactivateDockElement(DesktopWindow e) {
        if (e == null || dockElementMap.get(e.getConfig()) == null) {
            return;
        }
        WindowConfig config = e.getConfig();
        OsDockElement element = removeDockElement(config, false);
        if (element == null) {
            return;
        }
        element.removeClassName("active");
    }

    OsDockElement removeDockElement(WindowConfig config, boolean force) {
        OsDockElement element = dockElementMap.get(config);
        if (element == null) {
            return null;
        }
        element.setActiveInstance(null);
        if (element.getUserConfig() == null || force) {
            remove(element);
            element.removeFromParent();
            dockElementMap.remove(config);
            return null;
        }
        return element;
    }

    Map<WindowConfig, OsDockElement> getDockElementMap() {
        return dockElementMap;
    }
}
