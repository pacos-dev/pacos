package org.pacos.config.repository.data;

import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;

public record AppRepository(
        String id,
        String url) {

    public static AppRepository moduleRepo() {
        return new AppRepository("pacos-module-repo", ApplicationProperties.get()
                .getProperty(PropertyName.MODULE_LIST_REPO_URL.getPropertyName()));
    }

    public static AppRepository pluginRepo() {
        return new AppRepository("pacos-plugin-repo", ApplicationProperties.get()
                .getProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName()));
    }

    @Override
    public String toString() {
        return "AppRepository{" +
                "name='" + id + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
