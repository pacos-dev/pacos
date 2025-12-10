
package org.pacos.base.window.manager;

import java.io.Serializable;

import org.pacos.base.file.FileInfo;

/**
 * Application manager is responsible for finding a module that will be able to open the given file format.
 * After open a file, applicationManger make a decision which module/plugin/extension should be used to open a file.
 * <p>
 * Module/Plugin is able to open a file when {@link org.pacos.base.window.DesktopWindow} implements {@link org.pacos.base.window.FileOpenAllowed}
 * and {@link org.pacos.base.window.config.WindowConfig} is extended about {@link org.pacos.base.window.config.FileExtensionHandler}
 */
public interface ApplicationManager extends Serializable {

    /**
     * Find and open file in default module that support file extension.
     * If module is not active, ApplicationManager first initialize the module, and then open method is triggered
     * on active {@link org.pacos.base.window.DesktopWindow}
     */
    boolean open(FileInfo fileInfo);
}
