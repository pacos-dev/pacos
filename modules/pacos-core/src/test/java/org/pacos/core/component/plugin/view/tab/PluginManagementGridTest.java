package org.pacos.core.component.plugin.view.tab;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.service.PluginService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginManagementGridTest {

    private PluginProxy pluginProxy;
    private PluginManagementGrid pluginGrid;
    private PluginState pluginState;

    @BeforeEach
    public void setUp() {
        PluginService pluginService = Mockito.mock(PluginService.class);
        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        pluginProxy = Mockito.mock(PluginProxy.class);
        when(pluginProxy.getPluginService()).thenReturn(pluginService);
        when(pluginProxy.getPluginManager()).thenReturn(pluginManager);
        pluginState = new PluginState();
        when(pluginProxy.getPluginState()).thenReturn(pluginState);
        VaadinMock.mockSystem();
        this.pluginGrid = new PluginManagementGrid(pluginProxy, pluginManager);
    }

    @Test
    void whenGetStatusIconThenReturnComponent() {
        for (PluginStatusEnum status : PluginStatusEnum.values()) {
            assertNotNull(PluginManagementGrid.getStatusIcon(status));
        }
    }

    @Test
    void whenGetBtnTextThenReturnValue() {
        for (PluginStatusEnum status : PluginStatusEnum.values()) {
            assertNotNull(PluginManagementGrid.getButtonText(status));
        }
    }

    @Test
    void whenRenderIconColumnThenGetImager() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("pacos");
        pluginDTO.setGroupId("org.pacos");
        pluginDTO.setVersion("1.0.0");
        assertNotNull(pluginGrid.getImage(pluginDTO));
    }

    @Test
    void whenSetAutoStartToFalseThenDisablePluginAutoStart() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setDisabled(false);
        Checkbox checkbox = PluginManagementGrid.createCheckboxForAutoStartConfig(pluginProxy,pluginDTO);
        assertEquals(true, checkbox.getValue());
        checkbox.setValue(false);
        verify(pluginProxy.getPluginService()).disablePlugin(pluginDTO);
    }

    @Test
    void whenSetAutoStartToTrueThenEnablePluginAutoStart() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setDisabled(true);
        Checkbox checkbox = PluginManagementGrid.createCheckboxForAutoStartConfig(pluginProxy,pluginDTO);
        assertEquals(false, checkbox.getValue());
        checkbox.setValue(true);
        verify(pluginProxy.getPluginService()).enablePlugin(pluginDTO);
    }

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenPluginIsDisabledThenStartPlugin(PluginStatusEnum statusEnum){
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("org.pacos");
        pluginDTO.setArtifactName("pacos");
        pluginDTO.setVersion("1.0.0");
        pluginState.setState(pluginDTO,statusEnum);
        Button btn = pluginGrid.createStartStopButtonForPlugin(pluginDTO,statusEnum);
        btn.click();
        if(pluginState.getState(pluginDTO).canRun()){
            verify(pluginProxy.getPluginManager()).startPlugin(pluginDTO);
        }else{
            verify(pluginProxy.getPluginManager()).stopPlugin(pluginDTO);
        }

    }

}