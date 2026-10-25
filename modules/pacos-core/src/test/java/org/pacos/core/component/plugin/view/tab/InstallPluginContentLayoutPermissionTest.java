package org.pacos.core.component.plugin.view.tab;

import com.vaadin.flow.component.upload.Upload;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class InstallPluginContentLayoutPermissionTest {

    @Test
    void whenInstallUploadedLibraryWithoutPermissionThenDoNotInstallPlugin() {
        VaadinMock.mockSystem(new UserDTO(3, "limited", 0));
        PluginProxy pluginProxy = mock(PluginProxy.class);
        PluginManager pluginManager = mock(PluginManager.class);
        InstallPluginContentLayout layout = new InstallPluginContentLayout(pluginManager, pluginProxy);

        layout.installUploadedLibrary();

        assertFalse(layout.getChildren()
                .filter(Upload.class::isInstance)
                .map(Upload.class::cast)
                .findFirst()
                .orElseThrow()
                .isEnabled());
        verifyNoInteractions(pluginProxy, pluginManager);
    }
}