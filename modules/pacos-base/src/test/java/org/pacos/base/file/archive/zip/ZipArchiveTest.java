package org.pacos.base.file.archive.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipArchiveTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenTarFileThenCreateArchiveFile() throws IOException {
        //given
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "test content");
        //when
        Path tarGzFile = ZipArchive.archiveInParentDirectory(file);
        //then
        assertTrue(tarGzFile.toFile().exists());
        assertEquals(tempDir.resolve("test.zip"), tarGzFile);
        assertTrue(tarGzFile.toFile().length() > 1);
    }

    @Test
    void whenTarDirectoryThenCreateArchiveFile() throws IOException {
        //given
        Path dir = tempDir.resolve("test");
        dir.toFile().mkdirs();
        //when
        Path tarGzFile = ZipArchive.archiveInParentDirectory(dir);
        //then
        assertTrue(tarGzFile.toFile().exists());
        assertEquals(tempDir.resolve("test.zip"), tarGzFile);
        assertTrue(tarGzFile.toFile().length() > 50);
    }

    @Test
    void whenTarDirectoryWithFilesThenCreateArchiveFile() throws IOException {
        //given
        Path dir = tempDir.resolve("test");
        dir.toFile().mkdirs();
        Path file = dir.resolve("test.txt");
        Files.writeString(file, "test content");
        //when
        Path tarGzFile = ZipArchive.archiveInParentDirectory(dir);
        //then
        assertTrue(tarGzFile.toFile().exists());
        assertEquals(tempDir.resolve("test.zip"), tarGzFile);
        assertTrue(tarGzFile.toFile().length() > 100);
    }


    @Test
    void whenCompressAndUncompressThenFileContentIsValid() throws IOException {
        //given
        Path dir = tempDir.resolve("test");
        dir.toFile().mkdirs();
        Path file = dir.resolve("test.txt");
        final String testContent = "test content";
        Files.writeString(file, testContent);
        //when
        Path zipFile = ZipArchive.archiveInParentDirectory(dir);
        Path uncompressedPath = UnZipArchive.uncompress(zipFile, tempDir);
        //then
        assertTrue(uncompressedPath.toFile().exists());
        File[] filesUncompressed = uncompressedPath.toFile().listFiles();
        assertNotNull(filesUncompressed);
        assertEquals(1, filesUncompressed.length);
        assertTrue(filesUncompressed[0].isDirectory());

        filesUncompressed = filesUncompressed[0].listFiles();
        assertNotNull(filesUncompressed);
        assertEquals(1, filesUncompressed.length);
        assertTrue(filesUncompressed[0].isFile());

        assertEquals(testContent, Files.readString(filesUncompressed[0].toPath()));
    }
}