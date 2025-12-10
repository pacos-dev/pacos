package org.pacos.base.component.filepicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pacos.base.file.FileInfo;

public final class RootDirsLoader {

    private RootDirsLoader() {
    }

    static List<FileInfo> getRootDirLocation(List<FileInfo> filePath) {
        List<FileInfo> results = new ArrayList<>();

        if (filePath != null) {
            results = filePath.stream().filter(fp -> fp.getFile().exists()).toList();
        } else {
            File[] rootFiles = File.listRoots();
            for (File file : rootFiles) {
                results.add(FileInfo.of(file));
            }
        }
        return results;
    }
}
