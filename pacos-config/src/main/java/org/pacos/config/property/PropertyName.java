package org.pacos.config.property;

public enum PropertyName {

    MODULE_LIST_REPO_URL("module.list.repo.url"),
    WORKING_DIR("workingDir"),
    RMI_PORT("rmi.port"),
    VERSION("version"),
    SPRING_DATASOURCE_URL("spring.datasource.url"),
    SPRING_DATASOURCE_USER("spring.datasource.username"),
    SPRING_DATASOURCE_PASSWORD("spring.datasource.password"),
    PLUGIN_LIST_REPO_URL("plugin.list.repo.url");

    private final String name;

    PropertyName(String name) {
        this.name = name;
    }

    public String getPropertyName() {
        return name;
    }
}
