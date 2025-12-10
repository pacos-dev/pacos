package org.pacos.base.file.archive.gzip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.file.archive.ArchiveException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GzArchiveTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenGzipFileThenCreateArchiveFile() throws IOException {
        //given
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "test content");
        //when
        Path gzipGzFile = GzipArchive.archiveSingleFile(file);
        //then
        assertTrue(gzipGzFile.toFile().exists());
        assertEquals(tempDir.resolve("test.txt.gz"), gzipGzFile);
        assertTrue(gzipGzFile.toFile().length() > 1);
    }

    @Test
    void whenGzipDirectoryThenCreateArchiveFile() {
        //given
        Path dir = tempDir.resolve("test");
        dir.toFile().mkdirs();
        //when
        Throwable t = assertThrows(ArchiveException.class, () -> GzipArchive.archiveSingleFile(dir));
        //then
        assertEquals(t.getMessage(), "Can't gzip directory");
    }

}