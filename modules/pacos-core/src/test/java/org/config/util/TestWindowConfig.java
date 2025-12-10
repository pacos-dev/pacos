package org.config.util;

import java.util.List;

import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.FileExtensionHandler;
import org.pacos.base.window.config.WindowConfig;

public class TestWindowConfig implements WindowConfig, FileExtensionHandler {

    @Override
    public String title() {
        return "test";
    }

    @Override
    public String icon() {
        return "test";
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return TestWindow.class;
    }

    @Override
    public boolean isApplication() {
        return true;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return false;
    }

    @Override
    public List<String> allowedExtension() {
        return List.of("txt");
    }
}
