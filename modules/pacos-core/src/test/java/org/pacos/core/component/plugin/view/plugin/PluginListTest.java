package org.pacos.core.component.plugin.view.plugin;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

class PluginListTest {

    @Test
    void whenDisplayInstalledModeThenNoException() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginDetails details = Mockito.mock(PluginDetails.class);
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        PluginDTO inMarket = new PluginDTO(new Plugin("org.pacos", "test", "", "1.1", "", ""));

        PluginHandler handler = new PluginHandler(List.of(installed), List.of(inMarket),
                AppRepository.pluginRepo());
        //when
        assertDoesNotThrow(() -> new PluginList(pluginProxy, handler, details));
    }

    @Test
    void whenDisplayMarketPlaceModeThenNoException() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginDetails details = Mockito.mock(PluginDetails.class);

        PluginHandler handler = new PluginHandler(prepareInstalledList(), prepareMarketList(),
                AppRepository.pluginRepo());
        //when
        assertDoesNotThrow(() -> new PluginList(pluginProxy, handler, details));
    }

    @Test
    void whenRowClickedThenShowDetails() {
        PluginDetails details = Mockito.mock(PluginDetails.class);
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Button button = new Button();

        PluginList.onPluginRowClickEvent(details, installed, button);

        verify(details).showForPlugin(installed);
        verify(details).setBtnAndSynchronize(button);
    }

    @Test
    void whenSetStatusThenNoException() {
        PluginDetails details = Mockito.mock(PluginDetails.class);
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));

        for (DownloadPluginStatus status : DownloadPluginStatus.values()) {
            assertDoesNotThrow(() -> PluginList.setBtnStatus(installed, new Button(), details, status));

        }
    }

    private List<PluginDTO> prepareInstalledList() {
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        return List.of(installed);
    }

    private List<PluginDTO> prepareMarketList() {
        PluginDTO installed = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        PluginDTO newVersion = new PluginDTO(new Plugin("org.pacos", "test", "", "1.1", "", ""));
        PluginDTO newPlugin = new PluginDTO(new Plugin("org.pacos", "test2", "", "1.1", "", ""));
        return List.of(installed, newVersion, newPlugin);
    }
}