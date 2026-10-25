package org.pacos.core.component.plugin.view.tab;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginLifecycle;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.data.PluginStatus;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginDownloadState;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;

class PluginManagementTabLayoutTest {

    private PluginManager pluginManager;
    private PluginProxy pluginProxy;

    @BeforeEach
    void setUp() {
        PluginService pluginService = Mockito.mock(PluginService.class);
        pluginProxy = Mockito.mock(PluginProxy.class);
        Mockito.when(pluginProxy.getPluginState()).thenReturn(new org.pacos.core.component.plugin.manager.PluginState());
        VaadinMock.mockSystem();
        pluginManager = new PluginManager(pluginService, new PluginState(), Mockito.mock(PluginLifecycle.class));

    }

    @Test
    void whenNoPluginsThenManagementTabIsInitialized() {
        assertDoesNotThrow(() -> new PluginManagementTabLayout(pluginManager, pluginProxy));
    }

    @Test
    void whenPluginsStatusChangedThenRowIsUpdated() {
        //given
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0.0");
        pluginDTO.setGroupId("org.pacos");

        PluginManagementTabLayout pluginManagementTabLayout = spy(new PluginManagementTabLayout(pluginManager, pluginProxy));
        //when
        PluginStatus status = new PluginStatus(pluginDTO, PluginStatusEnum.INITIALIZATION);
        pluginManagementTabLayout.pluginStatusChanged(status);
        //then
        verify(pluginManagementTabLayout).refreshItem(pluginDTO);
    }

    @Test
    void whenPluginDownloadedThenRowIsAdded() {
        //given
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0.0");
        pluginDTO.setGroupId("org.pacos");

        PluginManagementTabLayout pluginManagementTabLayout = spy(new PluginManagementTabLayout(pluginManager, pluginProxy));
        //when
        PluginDownloadState status = new PluginDownloadState(pluginDTO, DownloadPluginStatus.DOWNLOADING);
        pluginManagementTabLayout.pluginDownloadedStatusChanged(status);
        //then
        verify(pluginManagementTabLayout).refreshItem(pluginDTO);

    }

}
