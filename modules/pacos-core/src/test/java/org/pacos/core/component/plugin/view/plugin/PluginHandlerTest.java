package org.pacos.core.component.plugin.view.plugin;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PluginHandlerTest {

    private final PluginDTO plugin1 = new PluginDTO(new Plugin("plugin1", "test", "", "1.0", "", ""));
    private final PluginDTO plugin2 = new PluginDTO(new Plugin("plugin2", "test", "", "2.0", "", ""));
    private final PluginDTO plugin3 = new PluginDTO(new Plugin("plugin3", "test", "", "3.0", "", ""));
    private final PluginDTO plugin4 = new PluginDTO(new Plugin("plugin4", "test", "", "4.0", "", ""));

    private final AppRepository repository = new AppRepository("1", "repo1");

    private final List<PluginDTO> installed = List.of(plugin1, plugin2);
    private final List<PluginDTO> marketPlace = List.of(plugin3, plugin4);

    private final PluginHandler pluginHandlerMarketplace = new PluginHandler(installed, marketPlace, repository);

    @Test
    void whenGetPluginToDisplayAndModeIsMarketplaceThenReturnMarketplaceList() {
        List<PluginDTO> result = pluginHandlerMarketplace.getPluginToDisplay();
        assertEquals(marketPlace, result);
    }
}
