package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.UI;
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
            dw.close();
            if (createdWindows.containsKey(dw.getConfig().activatorClass())) {
                createdWindows.get(dw.getConfig().activatorClass()).remove(dw);
                if (createdWindows.get(dw.getConfig().activatorClass()).isEmpty()) {
                    createdWindows.remove(dw.getConfig().activatorClass());
                }
            }
            windowQueue.remove(dw);
            //If manually programed close action is triggered, (and dif window is open in the same thread)
            //then the wrong window is moved to front
            Optional<DesktopWindow> nextOnFront = getWindowDisplayedOnFront();
            nextOnFront.ifPresent(DesktopWindow::moveToFront);
        } catch (Exception e) {
            NotificationUtils.error(e);
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
        for (Map.Entry<Class<?>, List<DesktopWindow>> entryWindow : createdWindows.entrySet()) {
            for (DesktopWindow window : entryWindow.getValue()) {
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

        DesktopWindow window;
        if (moduleConfig instanceof ModalWindowConfig config) {
            window = new ModalWindow(config);
        } else if (moduleConfig instanceof WarningWindowConfig config) {
            window = new WarningWindow(config);
        } else {

            window = springContext.getAutowireCapableBeanFactory().createBean(moduleConfig.activatorClass());

        }
        return showCreatedWindow(window);
    }

    Optional<DesktopWindow> getActiveWindow(WindowConfig config) {
        List<DesktopWindow> activeWindowOpt = createdWindows.get(config.activatorClass());
        return activeWindowOpt != null && !activeWindowOpt.isEmpty()
                ? Optional.ofNullable(activeWindowOpt.get(0)) : Optional.empty();
    }
}