package org.config.util;

import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;

public class TestWindow extends DesktopWindow {

    private boolean wasClosed = false;
    public TestWindow(WindowConfig moduleConfig) {
        super(moduleConfig);
    }

    @Override
    public void close() {
        wasClosed = true;
        super.close();
    }

    public boolean isWasClosed() {
        return wasClosed;
    }
}