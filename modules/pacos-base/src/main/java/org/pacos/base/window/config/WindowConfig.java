package org.pacos.base.window.config;

import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;

/**
 * Module window configuration.
 * Basic configuration required by the pacos system by all modal window
 */
public interface WindowConfig {

    /**
     * Return title that will be displayed in the header of the window
     */
    String title();

    /**
     * Icon that will be displayed in dock, application select and window header
     */
    String icon();

    /**
     * Module responsible for initialization of the module
     */
    Class<? extends DesktopWindow> activatorClass();

    /**
     * Return true if module can be visible in application list and could be stored in personal dock
     */
    boolean isApplication();

    /**
     * Return true if module can be visible in application list and could be stored in personal dock
     */
    boolean isAllowMultipleInstance();

    /**
     * Return true if module can be visible for current session
     * @return true by default
     */
    default boolean isAllowedForCurrentSession(UserSession userSession) {
        return true;
    }

    /**
     * True if window can be hide (minimize)
     *  @return true by default
     */
    default boolean isAllowedForMinimize() {
        return true;
    }

}
