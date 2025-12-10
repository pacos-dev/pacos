package org.pacos.core.component.registry.service;

public enum RegistryName {
    SINGLE_MODE, //Automatic login to admin account
    INSTALLED,// Defined if application was installed

    DEFAULT_DOCK_CONFIG, //ActivatorConfig.class
    GUEST_MODE, //Enable guest mode login
    REGISTRATION_PANEL, //Registration panel visible on index page
    LOG_PERMISSION,
    SYSTEM_VERSION, //Current system version installed
    RESTART_REQUIRED, //Display red label wheaten or not restart is required
    AVAILABLE_SYSTEM_VERSION, //Information about new version
    AVAILABLE_PLUGINS_COUNT_TO_UPDATE, //Count of plugins that can be updated
    LAST_UPDATE_TIME, //Time when last update was finished with success
    LAST_UPDATE_CHECK_TIME,
    AUTO_UPDATE_ENABLED,
    AUTO_UPDATE_PLUGIN_ENABLED
}
