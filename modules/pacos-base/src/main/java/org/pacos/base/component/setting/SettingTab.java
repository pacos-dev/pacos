package org.pacos.base.component.setting;

import org.pacos.base.session.UserSession;

/**
 * Provides template configuration settings for component that will be displayed in the system settings panel
 * <p>
 * PacOS will detect automatically each class that implements this interface, and will extend Setting module
 * accordingly.
 * The constructor can take any spring component. The class implements this interface will be managed by pacos and
 * pacos is responsible to inject dependencies
 */

public interface SettingTab {

    /**
     * Get header of the content
     */
    String getTitle();

    /**
     * Create component object.
     */
    SettingPageLayout generateContent();

    /**
     * Ordering for the given configuration
     */
    int getOrder();

    /**
     * Return true if settings tab can be displayed for current user session
     *
     * @return true by default
     */
    boolean shouldBeDisplayed(UserSession userSession);
}
