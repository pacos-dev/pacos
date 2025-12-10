package org.pacos.base.file.archive.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;
import org.pacos.base.file.archive.tar.UnTarGzArchive;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UnZipArchiveTest {

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
        Path file = new File("archive/test.zip").toPath();
        Path dest = tempDir.resolve("test");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(dest, file));
    }

    @Test
    void whenFileIsNotTarGzThenThrowException() throws IOException {
        //given
        Path dest = tempDir.resolve("test.txt");
        Files.writeString(dest, "content");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(dest, tempDir));
    }

    @Test
    void whenFileHasZipExtensionOnlyThenThrowException() throws IOException {
        //given
        Path dest = tempDir.resolve("test.zip");
        Files.writeString(dest, "content");
        assertThrows(ArchiveException.class, () -> UnZipArchive.uncompress(dest, tempDir));
    }

}