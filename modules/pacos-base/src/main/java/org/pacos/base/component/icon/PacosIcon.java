package org.pacos.base.component.icon;

/**
 * PacOS icon set
 * Call constructor from {@link com.vaadin.flow.component.html.Image} and pass value from PacosIcon.getUrl() as an arg
 */
public enum PacosIcon {

    USER_SETTING("user_setting"),
    ALERT("alert"),
    VARIABLE("variable"),
    UPLOAD("upload"),
    QUESTION("question"),
    FILE_ADD("file_add"),
    WEB("web"),
    SETTINGS("settings"),
    POWER_OFF("power_off"),
    LOGOUT("logout"),
    SYSTEM_INFO("system_info"),
    NO_IMAGE("noimage"),
    APP_STORE("app_store"),
    PACOS("pacos");

    private final String path;

    PacosIcon(String path) {
        this.path = path;
    }

    public String getUrl() {
        return "img/icon/" + path + ".png";
    }

}