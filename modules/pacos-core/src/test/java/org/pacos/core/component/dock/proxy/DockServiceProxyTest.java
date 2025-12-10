package org.pacos.core.component.dock.proxy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.dock.domain.DockConfiguration;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.service.DockService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DockServiceProxyTest {

    private DockService dockServiceMock;
    private DockServiceProxy dockServiceProxy;

    @BeforeEach
    void setUp() {
        dockServiceMock = mock(DockService.class);
        dockServiceProxy = new DockServiceProxy(dockServiceMock);
    }

    @Test
    void whenCalledLoadConfigurationsThenReturnMappedDTOs() {
        DockConfiguration dockConfig1 = new DockConfiguration("Activator1", 123);
        dockConfig1.setId(1);
        dockConfig1.setOrderNum(1);

        DockConfiguration dockConfig2 = new DockConfiguration("Activator2", 123);
        dockConfig2.setId(2);
        dockConfig2.setOrderNum(2);

        when(dockServiceMock.findByUserIdOrderByOrderNum(123)).thenReturn(List.of(dockConfig1, dockConfig2));

        List<DockConfigurationDTO> result = dockServiceProxy.loadConfigurations(123);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Activator1", result.get(0).getActivator());
        assertEquals(2, result.get(1).getId());
        assertEquals("Activator2", result.get(1).getActivator());
    }

    @Test
    void whenCalledAddActivatorThenReturnMappedDTO() {
        DockConfiguration dockConfig = new DockConfiguration("Activator1", 123);
        dockConfig.setId(1);
        dockConfig.setOrderNum(1);

        when(dockServiceMock.addActivator("Activator1", 123)).thenReturn(dockConfig);

        DockConfigurationDTO result = dockServiceProxy.addActivator("Activator1", 123);

        assertEquals(1, result.getId());
        assertEquals("Activator1", result.getActivator());
        assertEquals(123, result.getUserId());
        assertEquals(1, result.getOrderNum());
    }

    @Test
    void whenCalledRemoveActivatorThenDelegateToDockService() {
        dockServiceProxy.removeActivator("Activator1", 123);

        verify(dockServiceMock, times(1)).removeActivator("Activator1", 123);
    }
}
