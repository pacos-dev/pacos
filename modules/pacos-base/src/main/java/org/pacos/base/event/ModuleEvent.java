package org.pacos.base.event;

/**
 * PacOS event definition
 */
public enum ModuleEvent implements EventType {
    MODULE_SHUTDOWN,//DesktopWindow
    MODULE_OPENED,//DesktopWindow
    MODULE_REMOVED,//WindowConfig
    UPDATE_AVAILABLE,
    RESTART_REQUIRED,
    PLUGIN_INSTALL_STATE_CHANGED, //PluginStatus.class
    PLUGIN_INSTALLED, //PluginDTO
    PLUGIN_UNINSTALLED,  //PluginDTO
    PLUGIN_DOWNLOAD_STATE_CHANGED,
    VARIABLE_PROVIDER_ADDED,
    ACTIVE_WINDOW, //DesktopWindow
}
