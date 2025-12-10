package org.pacos.base.file.archive.tar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UnTarGzArchiveTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenSourceFileNotExistsThenThrowException() {
        //given
        Path file = tempDir.resolve("test.txt");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(file, tempDir));
    }

    @Test
    void whenDestinationDirNotExistsThenThrowException() {
        //given
        Path file = new File("archive/test.tar.gz").toPath();
        Path dest = tempDir.resolve("test");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(dest, file));
    }

    @Test
    void whenFileIsNotTarGzThenThrowException() throws IOException {
        //given
        Path dest = tempDir.resolve("test.txt");
        Files.writeString(dest, "content");
        assertThrows(ArchiveException.class, () -> UnTarGzArchive.uncompress(dest, tempDir));
    }

    @Test
    void whenFileHasTarGzExtensionOnlyThenThrowException() throws IOException {
        //given
        Path dest = tempDir.resolve("test.tar.gz");
        Files.writeString(dest, "content");
        assertThrows(ZipException.class, () -> UnTarGzArchive.uncompress(dest, tempDir));
    }
}