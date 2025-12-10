
package org.pacos.core.system.manager;

import java.io.Serializable;

import org.pacos.base.event.UISystem;
import org.pacos.base.file.FileInfo;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.FileOpenAllowed;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationManagerImpl implements ApplicationManager, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationManagerImpl.class);

    @Override
    public boolean open(FileInfo fileInfo) {
        String extension = fileInfo.getExtension();
        WindowConfig allowedToOpen = PluginResource.findWindowAllowedForExtension(fileInfo.getExtension(), UserSession.getCurrent());
        if (allowedToOpen != null) {
            final DesktopWindow desktopWindow =
                    UISystem.getCurrent().getWindowManager().showWindow(allowedToOpen.getClass());

            ((FileOpenAllowed) desktopWindow).openFile(fileInfo);
            return true;
        } else {
            LOG.debug("Extension not supported by application: {}. Can't open file {}", extension, fileInfo.getFile());
            return false;
        }
    }
}
