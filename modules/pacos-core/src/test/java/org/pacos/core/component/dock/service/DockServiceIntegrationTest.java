package org.pacos.core.component.dock.service;

import java.util.List;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.dock.domain.DockConfiguration;
import org.pacos.core.component.dock.repository.DockConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private DockService dockService;
    @Autowired
    private DockConfigurationRepository repository;

    @Test
    void whenAddActivatorThenEntityIsStoredInDB() {
        //when
        DockConfiguration activator = dockService.addActivator("org.pacos.test.class", 1);
        //then
        assertNotNull(activator.getId());
        assertEquals(1, repository.count());
    }

    @Test
    void whenRemoveActivatorThenRemoveFromDB() {
        //given
        dockService.addActivator("org.pacos.test.class", 1);
        dockService.addActivator("org.pacos.test.class2", 1);
        assertEquals(2, repository.count());
        //when
        dockService.removeActivator("org.pacos.test.class", 1);
        //then
        assertEquals(1, repository.count());
    }

    @Test
    void whenFindByUserIdThenReturnTwoResults() {
        //given
        dockService.addActivator("org.pacos.test.class", 1);
        DockConfiguration config1 = dockService.addActivator("org.pacos.test.class", 2);
        DockConfiguration config2 = dockService.addActivator("org.pacos.test.class2", 2);
        assertEquals(3, repository.count());
        //when
        List<DockConfiguration> configurations = dockService.findByUserIdOrderByOrderNum(2);
        //then
        assertEquals(2, configurations.size());
        assertTrue(configurations.contains(config1));
        assertTrue(configurations.contains(config2));
    }

    @Test
    void whenNoConfigurationThenReturnEmptyList() {
        //when
        List<DockConfiguration> configurations = dockService.findByUserIdOrderByOrderNum(3);
        //then
        assertTrue(configurations.isEmpty());
    }
}