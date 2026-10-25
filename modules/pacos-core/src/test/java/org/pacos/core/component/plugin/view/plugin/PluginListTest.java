package org.pacos.core.component.plugin.view.plugin;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.session.UserDTO;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
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
        assertDoesNotThrow(() -> new PluginList(pluginProxy, handler, details));
    }

    @Test
    void whenDisplayMarketPlaceModeThenNoException() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginDetails details = Mockito.mock(PluginDetails.class);

        PluginHandler handler = new PluginHandler(prepareInstalledList(), prepareMarketList(),
                AppRepository.pluginRepo());
        assertDoesNotThrow(() -> new PluginList(pluginProxy, handler, details));
    }

    @Test
    void whenInstallPluginWithoutPermissionThenDoNotStartDownload() {
        VaadinMock.mockSystem(new UserDTO(3, "limited", 0));
        PluginProxy pluginProxy = Mockito.mock(PluginProxy.class);
        PluginDTO plugin = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        PluginHandler handler = new PluginHandler(List.of(), List.of(plugin), AppRepository.pluginRepo());
        PluginList pluginList = new PluginList(pluginProxy, handler, Mockito.mock(PluginDetails.class));
        Button installButton = pluginList.getChildren()
                .flatMap(Component::getChildren)
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .findFirst()
                .orElseThrow();

        assertFalse(installButton.isEnabled());
        pluginList.installPlugin(plugin);

        verify(pluginProxy, never()).getPluginInstallService();
    }

    @Test
    void whenSetErrorStatusWithoutPermissionThenKeepButtonDisabled() {
        VaadinMock.mockSystem(new UserDTO(3, "limited", 0));
        PluginDTO plugin = new PluginDTO(new Plugin("org.pacos", "test", "", "1.0", "", ""));
        Button button = new Button();
        button.setEnabled(false);

        PluginList.setBtnStatus(plugin, button, Mockito.mock(PluginDetails.class), DownloadPluginStatus.ERROR);

        assertFalse(button.isEnabled());
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
