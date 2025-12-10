package org.pacos.base.event;

import java.io.Serializable;

import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.base.window.manager.ClipboardManager;
import org.pacos.base.window.manager.DownloadManager;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.manager.VariableManager;
import org.pacos.base.window.manager.WindowManager;

/**
 * Provides access to basic system functionality and communication between different part of the system
 */
public class UISystem extends SystemEvent<EventType> implements Serializable {
    private final VariableManager variableManager;
    private final WindowManager windowManager;
    private final ApplicationManager applicationManager;
    private final ShortcutManager shortcutManager;
    private final DownloadManager downloadManager;
    private final ClipboardManager clipboardManager;

    public UISystem(DownloadManager downloadLink,
                    VariableManager variableManager,
                    WindowManager windowManager,
                    ApplicationManager applicationManager,
                    ShortcutManager shortcutManager,
                    ClipboardManager clipboardManager) {
        this.downloadManager = downloadLink;
        this.variableManager = variableManager;
        this.windowManager = windowManager;
        this.applicationManager = applicationManager;
        this.shortcutManager = shortcutManager;
        this.clipboardManager = clipboardManager;
    }


    public VariableManager getVariableManager() {
        return variableManager;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public ShortcutManager getShortcutManager() {
        return shortcutManager;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }

    /**
     * Static access to current UISystem instance assigned to userSession
     */
    public static UISystem getCurrent() {
        return UserSession.getCurrent().getUISystem();
    }
}
