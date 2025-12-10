package org.pacos.base.window;

import org.pacos.base.file.FileInfo;

/**
 * DesktopWindow implements this interface in case when module provide file open action.
 * Allow file opening by the module from the explorer
 */
@FunctionalInterface
public interface FileOpenAllowed {

    void openFile(FileInfo fileInfo);
}
