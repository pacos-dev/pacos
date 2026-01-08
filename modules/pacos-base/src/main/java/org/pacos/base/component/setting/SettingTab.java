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
     * Returns the display name of the settings item.
     * This string will be used as the label in the navigation tree menu.
     * @return the menu item title.
     */
    String getTitle();

    /**
     * Generates a fresh instance of the settings page layout.
     * To ensure session isolation and prevent UI state conflicts,
     * this method must always return a new object instance (prototype)
     * rather than a singleton.
     * @return a new layout component for this setting page.
     */
    SettingPageLayout generateContent();

    /**
     * Returns the sorting priority for this item within its tree level.
     * Items with lower order values are displayed at the top of their group.
     * @return the sequence order in the tree menu.
     */
    int getOrder();

    /**
     * Return true if settings tab can be displayed for current user session
     *
     * @return true by default
     */
    boolean shouldBeDisplayed(UserSession userSession);

    /**
     * Defines the hierarchical path in the settings tree where this tab should be placed.
     * Each string in the array represents a subsequent nesting level (folder).
     * * @return an array of group names defining the path,
     * or {@code null}/empty array to place the tab at the root level.
     */
    default String[] getGroup() {
        return new String[]{};
    }

    /**
     * Returns a pre-aggregated and normalized string of keywords for the advanced search.
     * This index should include the title, labels, and relevant content of the tab.
     * * @return a lowercase string containing searchable content,
     * or {@code null} if the tab should be excluded from search results.
     */
    default String getSearchIndex() {
        return null;
    }
}
