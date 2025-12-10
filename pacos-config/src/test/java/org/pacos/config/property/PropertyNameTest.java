package org.pacos.config.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyNameTest {

    @Test
    void testGetPropertyName() {
        assertEquals("module.list.repo.url", PropertyName.MODULE_LIST_REPO_URL.getPropertyName());
        assertEquals("workingDir", PropertyName.WORKING_DIR.getPropertyName());
        assertEquals("spring.datasource.url", PropertyName.SPRING_DATASOURCE_URL.getPropertyName());
        assertEquals("spring.datasource.username", PropertyName.SPRING_DATASOURCE_USER.getPropertyName());
        assertEquals("spring.datasource.password", PropertyName.SPRING_DATASOURCE_PASSWORD.getPropertyName());
        assertEquals("plugin.list.repo.url", PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName());
        assertEquals("rmi.port", PropertyName.RMI_PORT.getPropertyName());
        assertEquals("version", PropertyName.VERSION.getPropertyName());
    }
}