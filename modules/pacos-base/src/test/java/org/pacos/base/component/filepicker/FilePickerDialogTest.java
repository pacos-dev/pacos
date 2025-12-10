package org.pacos.base.component.filepicker;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.exception.PacosException;
import org.pacos.base.file.FileInfo;
import org.pacos.base.mock.VaadinMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class FilePickerDialogTest {

    @TempDir
    private Path rootDir;

    @Test
    void whenRootDirsNotGivenThenThrowException() {
        assertThrows(PacosException.class, () -> new FilePickerDialog(null, true,null));
    }

    @Test
    void whenRootDirsGivenThenNoException() {
        VaadinMock.mockSystem();
        assertDoesNotThrow(() -> new FilePickerDialog(List.of(), true,null));
    }

    @Test
    void whenSetSelectedFileThenExpandAllTreeAndNotThrowException() {
        VaadinMock.mockSystem();
        Path leaf1 = rootDir.resolve("leaf1");
        Path leaf2 = leaf1.resolve("leaf2");
        if (!leaf2.toFile().mkdirs()) {
            fail();
        }
        FileInfo fi = FileInfo.of(rootDir.toFile());
        FilePickerDialog dialog = new FilePickerDialog(List.of(fi), true,null);
        assertDoesNotThrow(() -> dialog.setSelectedFile(leaf2));

    }
}