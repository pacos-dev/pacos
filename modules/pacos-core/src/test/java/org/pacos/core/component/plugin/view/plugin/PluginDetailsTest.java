package org.pacos.core.component.plugin.view.plugin;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;
import org.pacos.config.repository.info.PluginRelease;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PluginDetailsTest {

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
    }

    @Test
    void whenInitializeThenNoException() {
        assertDoesNotThrow(PluginDetails::new);
    }

    @Test
    void whenShowForPluginAndCantLoadPluginDetailsFromRepoThenNoException() {
        Plugin plugin = new Plugin("org.pacos", "test","test", "1.0","","");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        PluginDetails details = new PluginDetails();

        assertDoesNotThrow(() -> details.showForPlugin(pluginDTO));
    }

    @Test
    void whenShowForPluginAndDetailsLoadedThenNoException() {
        Plugin plugin = new Plugin("org.pacos", "test","test", "1.0","","");
        PluginDTO pluginDTO = new PluginDTO(plugin);
        pluginDTO.setPluginInfoDTO(new PluginInfo("name", "1.0", "icon", "test", "test",
                List.of(new PluginRelease("1.0", List.of("new feature")))));
        PluginDetails details = new PluginDetails();
        details.setBtnAndSynchronize(new Button());

        assertDoesNotThrow(() -> details.showForPlugin(pluginDTO));
        assertDoesNotThrow(() -> details.prepareViewContent(pluginDTO));
    }

}