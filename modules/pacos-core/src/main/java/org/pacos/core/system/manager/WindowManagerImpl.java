package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pacos.base.event.ModuleEvent;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.ModalWindow;
import org.pacos.base.window.WarningWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.config.impl.WarningWindowConfig;
import org.pacos.base.window.manager.WindowInitializingException;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.system.event.OpenWindowEvent;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.UI;

/**
 * Tracks open desktop windows in a user session. User-initiated shutdown may
 * be confirmed asynchronously; a window is removed from the registry only
 * after the window signals that its close has actually completed.
 */
public class WindowManagerImpl implements WindowManager, Serializable {

    private final Map<Class<?>, List<DesktopWindow>> createdWindows = new HashMap<>();
    private final LinkedHashSet<DesktopWindow> windowQueue = new LinkedHashSet<>();

    @Override
    public boolean isActive(WindowConfig config) {
        return getActiveWindow(config).isPresent();
    }

    @Override
    public void detachAll() {
        createdWindows.values().forEach(c -> c.forEach(DesktopWindow::close));
    }

    @Override
    public List<DesktopWindow> getAllInitializedWindows() {
        return createdWindows.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public void close(DesktopWindow dw) {
        try {
            dw.requestClose(() -> unregisterClosedWindow(dw));
        } catch (Exception exception) {
            NotificationUtils.error(exception);
        }
    }

    private void unregisterClosedWindow(DesktopWindow window) {
        List<DesktopWindow> instances = createdWindows.get(window.getConfig().activatorClass());
        if (instances != null) {
            instances.remove(window);
            if (instances.isEmpty()) {
                createdWindows.remove(window.getConfig().activatorClass());
            }
        }
        if (windowQueue.remove(window)) {
            getWindowDisplayedOnFront().ifPresent(DesktopWindow::moveToFront);
        }
    }

    @Override
    public <T extends WindowConfig> DesktopWindow showWindow(Class<T> clazz) throws WindowInitializingException {
        try {
            var springContext = PluginResource.getModuleContextForWindowConfig(clazz);
            var moduleConfig = springContext.getAutowireCapableBeanFactory().getBean(clazz);

            return initializeAndShow(moduleConfig, springContext);
        } catch (Exception e) {
            throw new WindowInitializingException(e);
        }
    }

    @Override
    public DesktopWindow showWindow(DesktopWindow window) throws WindowInitializingException {
        DesktopWindow dw = manageExistingWindow(window.getConfig());
        if (dw != null) {
            return dw;
        }
        return showCreatedWindow(window);
    }

    public <T extends WindowConfig> DesktopWindow showWindow(Class<T> clazz, ApplicationContext context)
            throws WindowInitializingException {
        try {
            WindowConfig moduleConfig = context.getAutowireCapableBeanFactory().getBean(clazz);

            DesktopWindow dw = manageExistingWindow(moduleConfig);
            if (dw != null) {
                return dw;
            }

            return initializeAndShow(moduleConfig, context);
        } catch (Exception e) {
            throw new WindowInitializingException(e);
        }
    }

    @Override
    public DesktopWindow showModalWindow(ModalWindowConfig moduleConfig) {
        DesktopWindow dw = manageExistingWindow(moduleConfig);
        if (dw != null) {
            return dw;
        }
        dw = new ModalWindow(moduleConfig);
        return showCreatedWindow(dw);
    }

    @Override
    public DesktopWindow showWarningWindow(WarningWindowConfig windowConfig) {
        DesktopWindow dw = manageExistingWindow(windowConfig);
        if (dw != null) {
            return dw;
        }
        dw = new WarningWindow(windowConfig);
        return showCreatedWindow(dw);
    }

    @Override
    public void showAndMoveToFront(DesktopWindow desktopWindow) {
        showOrHideAlreadyCreatedWindow(desktopWindow, true);
    }

    @Override
    public void showOrHideAlreadyCreatedWindow(DesktopWindow dw, boolean forceShow) {
        if (!dw.isOpened()) {
            OpenWindowEvent.fireEvent(dw, UI.getCurrent());
        } else {
            Optional<DesktopWindow> windowDisplayedOnFront = getWindowDisplayedOnFront();
            if (!forceShow) {
                if(windowDisplayedOnFront.isPresent() && dw.equals(windowDisplayedOnFront.get())) {
                    dw.minimize();
                    //remove from tree to force detach event
                    dw.getElement().removeFromTree();
                }else{
                    dw.moveToFront();
                }
            } else if(windowDisplayedOnFront.isPresent() && !dw.equals(windowDisplayedOnFront.get())) {
                dw.moveToFront();
            }
        }
    }

    @Override
    public List<DesktopWindow> getInitializedWindows(WindowConfig moduleConfig) {
        return createdWindows.get(moduleConfig.activatorClass());
    }

    @Override
    public List<DesktopWindow> getInitializedWindowsOfClass(Class<?> windowClass) {
        return createdWindows.get(windowClass);
    }

    @Override
    public Optional<DesktopWindow> getWindowDisplayedOnFront() {
        if (windowQueue.isEmpty()) {
            return Optional.empty();
        } else {
            DesktopWindow last = null;
            for (DesktopWindow elem : windowQueue) {
                last = elem;
            }
            return Optional.ofNullable(last);
        }
    }

    /**
     * Called by client when switch window on browser
     */
    @Override
    public void markWindowOnTop(DesktopWindow dw) {
        windowQueue.remove(dw);
        windowQueue.add(dw);
    }

    @Override
    public void closeAllInstances(WindowConfig windowConfig) {
        for (DesktopWindow window : getAllInitializedWindows()) {
            if (windowConfig.equals(window.getConfig())) {
                //close in ui if window is attached
                if (window.getUI().isPresent()) {
                    window.getUI().ifPresent(ui -> ui.access(() -> close(window)));
                } else {
                    close(window);
                }
            }
        }

    }

    private DesktopWindow showCreatedWindow(DesktopWindow window) {
        OpenWindowEvent.fireEvent(window, UI.getCurrent());
        createdWindows.computeIfAbsent(window.getClass(), e -> new ArrayList<>()).add(window);
        UserSession.getCurrent().getUISystem().notify(ModuleEvent.MODULE_OPENED, window);
        markWindowOnTop(window);
        return window;
    }

    private DesktopWindow manageExistingWindow(WindowConfig moduleConfig) {
        final Optional<DesktopWindow> activeWindow = getActiveWindow(moduleConfig);
        if (!moduleConfig.isAllowMultipleInstance() && activeWindow.isPresent()) {
            final DesktopWindow dw = activeWindow.get();
            if (moduleConfig.isAllowedForMinimize()) {
                showOrHideAlreadyCreatedWindow(dw, false);
            } else {
                dw.moveToFront();
            }
            return dw;
        }
        return null;
    }

    private <T extends WindowConfig> DesktopWindow initializeAndShow(T moduleConfig, ApplicationContext springContext) {
        DesktopWindow dw = manageExistingWindow(moduleConfig);
        if (dw != null) {
            return dw;
        }

        DesktopWindow window = switch (moduleConfig) {
            case ModalWindowConfig config -> new ModalWindow(config);
            case WarningWindowConfig config -> new WarningWindow(config);
            default -> springContext.getAutowireCapableBeanFactory().createBean(moduleConfig.activatorClass());
        };
        return showCreatedWindow(window);
    }

    Optional<DesktopWindow> getActiveWindow(WindowConfig config) {
        List<DesktopWindow> activeWindowOpt = createdWindows.get(config.activatorClass());
        return activeWindowOpt != null && !activeWindowOpt.isEmpty()
                ? Optional.ofNullable(activeWindowOpt.getFirst()) : Optional.empty();
    }
}