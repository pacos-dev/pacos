package org.pacos.base.window.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.config.impl.WarningWindowConfig;
import org.springframework.context.ApplicationContext;

/**
 * Introduces window management functionality
 */
public interface WindowManager extends Serializable {

    /**
     * Return true if at least one window is already initialized for the given configuration
     */
    boolean isActive(WindowConfig config);

    /**
     * Called once user session is destroyed.
     * Ensures that every open window receives a detach event
     */
    void detachAll();

    /**
     * Return list of currently running apps
     */
    List<DesktopWindow> getAllInitializedWindows();

    /**
     * Used for initializing {@link org.pacos.base.window.ModalWindow}
     * When using this constructor the class does not need to be declared as a Spring component
     */
    DesktopWindow showModalWindow(ModalWindowConfig windowConfig);

    /**
     * Used for initializing {@link org.pacos.base.window.WarningWindow}.
     * When using this constructor the class does not need to be declared as a Spring component
     */
    DesktopWindow showWarningWindow(WarningWindowConfig windowConfig);

    /**
     * Used for initializing {@link DesktopWindow} based on WindowConfig declared as a singleton spring component
     * <p>
     * In this case the configuration is a singleton because it concerns the main window of the module/extension which
     * does not change over time.
     * - If the window has already been initialized and is visible on front, then triggering this method will minimize the window.
     * - If a window is displayed but is covered by another window, it will be moved to the front
     * - If the window is already minimized, then triggering this method will restore the window on front.
     * - If the window does not exist, then it will be initialized and displayed.
     * <p>
     * @throws WindowInitializingException if window can't be initialized
     */
    <T extends WindowConfig> DesktopWindow showWindow(Class<T> windowConfigClass) throws WindowInitializingException;

    <T extends WindowConfig> DesktopWindow showWindow(Class<T> windowConfigClass, ApplicationContext context) throws WindowInitializingException;

    /**
     * Show window manually created. Useful for windows that can have multiple instance
     */
    DesktopWindow showWindow(DesktopWindow window) throws WindowInitializingException;

    /**
     * Close window and all child windows
     */
    void close(DesktopWindow dw);

    /**
     * Return all initialized windows for the given configuration
     */
    List<DesktopWindow> getInitializedWindows(WindowConfig moduleConfig);

    List<DesktopWindow> getInitializedWindowsOfClass(Class<?> windowClass);

    /**
     * Move window to front if is opened and forceShow is set to true
     * Move window to front if is opened but not visible on front
     * Minimize if window is already visible on front and forceShow is set to false
     * If window is minimized then open and move to front
     */
    void showOrHideAlreadyCreatedWindow(DesktopWindow dw, boolean forceShow);

    /**
     * If window is hidden, then show as first on front,
     * If window is visible then move to front
     */
    void showAndMoveToFront(DesktopWindow desktopWindow);

    /**
     * Return active window displayed on front with highest zIndex
     */
    Optional<DesktopWindow> getWindowDisplayedOnFront();

    /**
     * Close all existing Window instances of given config.
     * Called when module was removed from the system
     */
    void closeAllInstances(WindowConfig e);

    /**
     * Used by callback from the client. Inform backend which window is currently displayed on top
     */
    void markWindowOnTop(DesktopWindow dw);
}
