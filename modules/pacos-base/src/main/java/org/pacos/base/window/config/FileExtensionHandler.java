package org.pacos.base.window.config;

import java.util.List;

import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.FileOpenAllowed;

/**
 * Extension for basic configuration of {@link WindowConfig}
 * {@link WindowConfig} should be extended about this interface in case when window will be able to read/open a file.
 * <p>
 * If this interface is used, then {@link DesktopWindow} assigned to {@link WindowConfig} must
 * implement {@link FileOpenAllowed}
 */
@FunctionalInterface
public interface FileExtensionHandler {

    /**
     * Return a list of extensions that a given module supports and for which it has an initializer method
     */
    List<String> allowedExtension();
}
