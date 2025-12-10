package org.pacos.core.component.plugin.view.tab;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.SwaggerUIConfigReload;
import org.pacos.core.component.plugin.manager.data.PluginStatus;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginDownloadState;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class PluginManagementTabLayoutTest {

    private PluginManager pluginManager;
    private PluginProxy pluginProxy;
    private SwaggerUIConfigReload swaggerUIConfigReload;

    @BeforeEach
    public void setUp() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        PluginService pluginService = Mockito.mock(PluginService.class);
        pluginProxy = Mockito.mock(PluginProxy.class);
        swaggerUIConfigReload = Mockito.mock(SwaggerUIConfigReload.class);
        VaadinMock.mockSystem();
        pluginManager = new PluginManager(pluginService, swaggerUIConfigReload, applicationContext);

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